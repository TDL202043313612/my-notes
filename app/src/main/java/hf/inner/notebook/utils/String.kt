package hf.inner.notebook.utils

fun String.getFilenameExtension() = substring(lastIndexOf(".") + 1)

fun String.getFilenameWithoutExtension() = substringBeforeLast(".")

fun String.isVideoFast() = Constant.VIDE_EXTENSIONS.any { endsWith(it, true) }
fun String.isImageFast() = Constant.PHOTO_EXTENSIONS.any { endsWith(it, true) }
fun String.isAudioFast() = Constant.AUDIO_EXTENSIONS.any { endsWith(it, true) }

