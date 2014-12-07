package photogift.server.service;


import org.springframework.util.Assert;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class PagedResult<T> implements Iterable<T> {

    private ArrayList<T> content;
    private String current;
    private String next;

    public PagedResult(ArrayList<T> content, @Nullable String current, String next) {
        Assert.notNull(content);
        Assert.notNull(next);

        this.content = content;
        this.current = current;
        this.next = next;
    }

    public ArrayList<T> getContent() {
        return content;
    }

    public String getCurrent() {
        return current;
    }

    public String getNext() {
        return next;
    }

    public void reverse() {
        String temp = current;
        current = next;
        next = temp;
        Collections.reverse(content);
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }
}
