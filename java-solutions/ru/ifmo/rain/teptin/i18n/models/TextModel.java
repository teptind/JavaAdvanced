package ru.ifmo.rain.teptin.i18n.models;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public abstract class TextModel extends BaseModel {
    protected String text;
    protected Collator collator;

    public String getText() {
        return text;
    }

    @Override
    public int compareTo(BaseModel o) {
        return collator.compare(text, ((TextModel)o).text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TextModel textModel = (TextModel) o;
        return text.equals(textModel.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), text);
    }

    @Override
    public String getAsString(Locale locale) {
        return text;
    }
}
