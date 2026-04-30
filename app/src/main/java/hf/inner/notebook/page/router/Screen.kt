package hf.inner.notebook.page.router

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Main : Screen()
    @Serializable
    data class CommentList(val parentNoteId: Long) : Screen()
    @Serializable
    data object Explore : Screen()

    @Serializable
    data class InputDetail(val id: Long) : Screen()

    @Serializable
    data object TagList : Screen()
    @Serializable
    data class TagDetail(val tag: String) : Screen()
    @Serializable
    data object Search : Screen()
    @Serializable
    data class Share(val id: Long) : Screen()
    @Serializable
    data class DateRangePage(val startTime: Long, val endTime: Long) : Screen()


    @Serializable
    data class YearDetail(val year: String) : Screen()
    @Serializable
    data class PictureDisplay(val pathList: List<String>, val curIndex: Int, val timestamps: List<Long>) : Screen()

    @Serializable
    data object DataManager : Screen()

    @Serializable
    data object RandomWalk : Screen()

    @Serializable
    data object Gallery : Screen()
}