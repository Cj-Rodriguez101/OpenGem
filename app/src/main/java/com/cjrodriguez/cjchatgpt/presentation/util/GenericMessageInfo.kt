package com.cjrodriguez.cjchatgpt.presentation.util

class GenericMessageInfo
private constructor(builder: Builder) {

    // required
    val id: String
    val title: String
    val uiComponentType: UIComponentType

    // optional
    val onDismiss: () -> Unit
    val description: String?
    val extraMessage: String?
    val positiveAction: PositiveAction?
    val negativeAction: NegativeAction?

    init {
        if (builder.id == null) {
            throw Exception("GenericDialog id cannot be null.")
        }
        if (builder.title == null) {
            throw Exception("GenericDialog title cannot be null.")
        }
        if (builder.uiComponentType == null) {
            throw Exception("GenericDialog uiComponentType cannot be null.")
        }
        this.id = builder.id!!
        this.title = builder.title!!
        this.uiComponentType = builder.uiComponentType!!
        this.onDismiss = builder.onDismiss
        this.extraMessage = builder.extraMessage
        this.description = builder.description
        this.positiveAction = builder.positiveAction
        this.negativeAction = builder.negativeAction
    }

    class Builder {

        var id: String? = null
            private set

        var title: String? = null
            private set

        var onDismiss: () -> Unit = { }
            private set

        var uiComponentType: UIComponentType? = UIComponentType.None
            private set

        var description: String? = null
            private set

        var extraMessage: String? = null
            private set

        var positiveAction: PositiveAction? = null
            private set

        var negativeAction: NegativeAction? = null
            private set

        fun id(id: String): Builder {
            this.id = id
            return this
        }

        fun title(title: String): Builder {
            this.title = title
            return this
        }

        fun uiComponentType(
            uiComponentType: UIComponentType
        ): Builder {
            this.uiComponentType = uiComponentType
            return this
        }

        fun description(
            description: String
        ): Builder {
            this.description = description
            return this
        }

        fun extraMessage(
            extraMessage: String
        ): Builder {
            this.extraMessage = extraMessage
            return this
        }

        fun positive(
            positiveAction: PositiveAction?,
        ): Builder {
            this.positiveAction = positiveAction
            return this
        }

        fun negative(
            negativeAction: NegativeAction
        ): Builder {
            this.negativeAction = negativeAction
            return this
        }

        fun build() = GenericMessageInfo(this)
    }
}

data class PositiveAction(
    val positiveBtnTxt: String,
    val onPositiveAction: () -> Unit = {},
)

data class NegativeAction(
    val negativeBtnTxt: String,
    val onNegativeAction: () -> Unit = {},
)