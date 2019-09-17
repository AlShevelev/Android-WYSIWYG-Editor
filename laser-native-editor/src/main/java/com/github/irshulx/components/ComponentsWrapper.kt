package com.github.irshulx.components

import com.github.irshulx.components.input.InputExtensions

class ComponentsWrapper(
        val inputExtensions: InputExtensions?,
        val htmlExtensions: HTMLExtensions?,
        val imageExtensions: ImageExtensions?) {

    class Builder {
        private var inputExtensions: InputExtensions? = null
        private var dividerExtensions: DividerExtensions? = null
        private var htmlExtensions: HTMLExtensions? = null
        private var imageExtensions: ImageExtensions? = null

        fun inputExtensions(inputExtensions: InputExtensions): Builder {
            this.inputExtensions = inputExtensions
            return this
        }

        fun htmlExtensions(htmlExtensions: HTMLExtensions): Builder {
            this.htmlExtensions = htmlExtensions
            return this
        }

        fun imageExtensions(imageExtensions: ImageExtensions): Builder {
            this.imageExtensions = imageExtensions
            return this
        }

        fun dividerExtensions(dividerExtensions: DividerExtensions): Builder {
            this.dividerExtensions = dividerExtensions
            return this
        }

        fun build(): ComponentsWrapper {
            return ComponentsWrapper(
                this.inputExtensions,
                this.htmlExtensions,
                this.imageExtensions)
        }
    }
}
