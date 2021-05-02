/**
 * Copyright (c) 2021 apsoftware
 * All rights reserved
 *
 * Special thanks to Bengt Cyren from which this code is based upon.
 */

import java.io.*

class FilmWorksToJpeg {
    companion object {
        private fun readSize(stream : InputStream) : Int {
            val msb = stream.read()
            val lsb = stream.read()

            // Size does not include the two bytes of the size field.
            return (msb shl 8) + lsb - 2
        }

        private fun writeSize(stream : OutputStream, size : Int) {
            // Block includes the size of these two bytes
            val blockSize = size + 2

            val ba = byteArrayOf(
                (blockSize shr 8).toByte(),         // MSB
                (blockSize and 0xFF).toByte()       // LSB
            )

            stream.write(ba)
        }

        private fun convertHeader(inStream : InputStream, outStream : OutputStream) : Boolean {
            // Consume the header
            val header = ByteArray(FilmWorksDefines.SFWHEAD)
            val len = inStream.read(header)

            if (len == FilmWorksDefines.SFWHEAD) {
                // Expect the SOI here
                val b1 = inStream.read()
                val b2 = inStream.read()

                if (b1 == 0xFF && b2 == FilmWorksDefines.SOI) {
                    // Write the SOI
                    outStream.write(0xFF)
                    outStream.write(0xD8)
                    return true
                }
            }

            return false
        }

        /**
         * Determine if the file has a SFW header
         * @return true if the file has a SFW header
         */
        fun isSFW(inputStream : InputStream) : Boolean {
            val isValid : Boolean
            val outputStream = ByteArrayOutputStream(16)

            try {
                isValid = convertHeader(inputStream, outputStream)
            } finally {
                inputStream.close()
                outputStream.close()
            }

            return isValid
        }

        fun convert(inStream : InputStream, outStream : OutputStream) : Boolean {
            var success = false

            if (convertHeader(inStream, outStream)) {
                do {
                    var byte = inStream.read()

                    // Scan up to the marker (Avoids potential size confusion)
                    while (byte != 0xFF) {
                        byte = inStream.read()
                    }
                    byte = inStream.read()

                    when (byte) {
                        FilmWorksDefines.APP0 -> {
                            val size = readSize(inStream)

                            // Consume Size bytes
                            for (i in 1..size) {
                                inStream.read()
                            }

                            // Write a JFIF block
                            outStream.write(FilmWorksDefines.sApp0)
                        }

                        FilmWorksDefines.DQT -> {
                            // Write the correct marker
                            val marker : ByteArray = byteArrayOf(0xFF.toByte(), 0xDB.toByte())
                            outStream.write(marker)

                            val size = readSize(inStream)
                            writeSize(outStream, size)

                            // Transfer Size bytes
                            for (i in 1..size) {
                                outStream.write(inStream.read())
                            }
                        }
                        FilmWorksDefines.SOF0 -> {
                            // Write the correct marker
                            val marker : ByteArray = byteArrayOf(0xFF.toByte(), 0xC0.toByte())
                            outStream.write(marker)

                            val size = readSize(inStream)
                            writeSize(outStream, size)

                            // Transfer Size bytes
                            for (i in 1..size) {
                                outStream.write(inStream.read())
                            }

                            // Insert missing DHT's
                            outStream.write(FilmWorksDefines.sDht)
                        }
                        FilmWorksDefines.SOS -> {
                            // Write the correct marker
                            val marker : ByteArray = byteArrayOf(0xFF.toByte(), 0xDA.toByte())
                            outStream.write(marker)

                            // Write up to the EOI marker
                            var b1 = inStream.read()
                            var b2 = inStream.read()
                            var size = 2

                            while (b1 != 0xFF || b2 != FilmWorksDefines.EOI) {
                                outStream.write(b1)

                                b1 = b2
                                b2 = inStream.read()
                                size++
                            }

                            // Append the EOI marker
                            val eoiMarker : ByteArray = byteArrayOf(0xFF.toByte(), 0xD9.toByte())
                            outStream.write(eoiMarker)

                            success = true
                            break
                        }

                        FilmWorksDefines.EOI -> {
                            println("SOS Not Found")
                            break
                        }

                        else -> {
                            println("Conversion Failed")
                            break
                        }
                    }
                } while (true)
            }

            return success
        }
    }
}