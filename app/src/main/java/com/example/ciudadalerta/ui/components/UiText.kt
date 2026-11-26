package com.example.ciudadalerta.ui.components

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

/**
 * Representación desacoplada de texto de UI que permite a los ViewModels emitir
 * mensajes localizados sin depender de Context.
 */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    data class StringResource(
        @param:StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText {
        constructor(@StringRes resId: Int, vararg args: Any) : this(resId, args.toList())
    }

    data class PluralResource(
        @param:PluralsRes val resId: Int,
        val quantity: Int,
        val args: List<Any> = emptyList()
    ) : UiText {
        constructor(@PluralsRes resId: Int, quantity: Int, vararg args: Any) :
            this(resId, quantity, args.toList())
    }

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is StringResource -> {
            val resolved = args.map { if (it is UiText) it.asString() else it }.toTypedArray()
            if (resolved.isEmpty()) stringResource(resId) else stringResource(resId, *resolved)
        }
        is PluralResource -> {
            val resolved = args.map { if (it is UiText) it.asString() else it }.toTypedArray()
            if (resolved.isEmpty()) {
                pluralStringResource(resId, quantity)
            } else {
                pluralStringResource(resId, quantity, *resolved)
            }
        }
    }

    fun asString(context: Context): String = when (this) {
        is DynamicString -> value
        is StringResource -> {
            val resolved = args.map { if (it is UiText) it.asString(context) else it }.toTypedArray()
            if (resolved.isEmpty()) context.getString(resId) else context.getString(resId, *resolved)
        }
        is PluralResource -> {
            val resolved = args.map { if (it is UiText) it.asString(context) else it }.toTypedArray()
            if (resolved.isEmpty()) {
                context.resources.getQuantityString(resId, quantity)
            } else {
                context.resources.getQuantityString(resId, quantity, *resolved)
            }
        }
    }
}
