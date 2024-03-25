package com.myplex.model;


import com.myplex.model.SetupBox;

import java.io.Serializable;
import java.util.List;

public class SetupBoxListResponse implements Serializable {
    public List<SetupBox> setupBoxList;

    @Override
    public String toString() {
        return setupBoxList.toString();
    }
}
