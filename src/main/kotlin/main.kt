/**
 * Copyright (c) 2021 apsoftware
 * All rights reserved
 */

import org.apache.commons.cli.*
import org.apache.commons.io.filefilter.WildcardFileFilter
import java.io.*

fun main(args: Array<String>) {
    println("SFW to JPEG Convertor")
    processCommandline(args)
}

fun processCommandline(args: Array<String>) {
    val options = Options()

    options.addOption("c", "convert", true, "[C]onvert file(s)")
    options.addOption("t", "test", true, "[T]est file(s)")
    options.addOption("h", "help", false, "Show [H]elp")

    val parser: CommandLineParser = DefaultParser()

    try {
        val cmd: CommandLine = parser.parse(options, args)

        when {
            cmd.hasOption('c') -> convert(getMatchingFiles(cmd.getOptionValue('c')))
            cmd.hasOption('t') -> test(getMatchingFiles(cmd.getOptionValue('t')))
            cmd.hasOption('h') -> showHelp(options)

            else -> {
                // No options specified
                if (args.size == 0) {
                    showHelp(options)
                } else {
                    // There may however be "things" on the commandline.  Let's see if they are SFW files.
                    for (arg in args) {
                        convert(getMatchingFiles(arg))
                    }
                }
            }
        }
    } catch (e : ParseException) {
        println("Invalid arguments.")
        showHelp(options)
    }
}

private fun showHelp(options : Options) {
    val formatter = HelpFormatter()
    formatter.printHelp("java -jar SFW.jar", options)
}

private fun getMatchingFiles(arg : String) : List<File> {
    val dir = File(".")
    val fileFilter: FileFilter = WildcardFileFilter(arg)
    val files = dir.listFiles(fileFilter)

    return files?.toList() ?: arrayListOf()
}

private fun test(files : List<File>) {
    println("Checking ${files.size} files")
    for (file in files) {
        print(" * " + file.name + "... ")
        if (FilmWorksToJpeg.isSFW(FileInputStream(file))) {
            println("yes")
        } else {
            println("no.")
        }
    }
}

private fun convert(files : List<File>) : Int {
    var count = 0
    for (file in files) {
        print(" * " + file.name + "... ")
        if (FilmWorksToJpeg.isSFW(FileInputStream(file)) && convert(file)) {
            println("succeeded")
            count++
        } else {
            println("failed.")
        }
    }

    return count
}

private fun convert(file : File) : Boolean {
    var success = false
    var inputStream : InputStream? = null
    var outputStream : OutputStream? = null

    try {
        inputStream = FileInputStream(file)
        outputStream = FileOutputStream(file.name + ".jpg")

        success = FilmWorksToJpeg.convert(inputStream, outputStream)
    } catch (e : FileNotFoundException) {
    } finally {
        inputStream?.close()
        outputStream?.close()
    }

    return success
}
