package com.github.irshulx.components

class ComponentsWrapper(
    val inputExtensions: InputExtensions?,
    val dividerExtensions: DividerExtensions?,
    val htmlExtensions: HTMLExtensions?,
    val imageExtensions: ImageExtensions?,
    val listItemExtensions: ListItemExtensions?,
    val mapExtensions: MapExtensions?,
    val macroExtensions: MacroExtensions?) {

    class Builder {
        private var inputExtensions: InputExtensions? = null
        private var dividerExtensions: DividerExtensions? = null
        private var htmlExtensions: HTMLExtensions? = null
        private var imageExtensions: ImageExtensions? = null
        private var listItemExtensions: ListItemExtensions? = null
        private var mapExtensions: MapExtensions? = null
        private var macroExtensions: MacroExtensions? = null

        fun inputExtensions(inputExtensions: InputExtensions): Builder {
            this.inputExtensions = inputExtensions
            return this
        }

        fun htmlExtensions(htmlExtensions: HTMLExtensions): Builder {
            this.htmlExtensions = htmlExtensions
            return this
        }

        fun listItemExtensions(listItemExtensions: ListItemExtensions): Builder {
            this.listItemExtensions = listItemExtensions
            return this
        }

        fun mapExtensions(mapExtensions: MapExtensions): Builder {
            this.mapExtensions = mapExtensions
            return this
        }

        fun imageExtensions(imageExtensions: ImageExtensions): Builder {
            this.imageExtensions = imageExtensions
            return this
        }

        fun macroExtensions(macroExtensions: MacroExtensions): Builder {
            this.macroExtensions = macroExtensions
            return this
        }

        fun dividerExtensions(dividerExtensions: DividerExtensions): Builder {
            this.dividerExtensions = dividerExtensions
            return this
        }

        fun build(): ComponentsWrapper {
            return ComponentsWrapper(
                this.inputExtensions,
                this.dividerExtensions,
                this.htmlExtensions,
                this.imageExtensions,
                this.listItemExtensions,
                this.mapExtensions,
                this.macroExtensions)
        }

    }
}
