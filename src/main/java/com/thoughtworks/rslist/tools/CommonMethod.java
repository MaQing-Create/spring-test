package com.thoughtworks.rslist.tools;

import com.thoughtworks.rslist.exception.RequestNotValidException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommonMethod {

    public <T> T getElementFromList(int index, List<T> list) {
        if (index < 1 || index > list.size()) {
            throw new RequestNotValidException("invalid index");
        }
        return list.get(index - 1);
    }

}
