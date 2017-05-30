package com.tochoose.impossible.mymemo;

import java.util.Date;

/**
 * Created by conscious on 2017-05-30.
 */

public class Memo {

    private String key, txt, title;
    private Date createDate, updateDate;


    public String getTitle() {
        if(txt!=null){
            if(txt.indexOf("\n")>-1){
                return txt.substring(0,txt.indexOf("\n"));
            } else{
                return txt;
            }
        }
        return title;

    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
