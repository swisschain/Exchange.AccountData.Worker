fun String.removeExtraWhitespaces(): String  {
    return  this.replace("\\s".toRegex(), " ")
}