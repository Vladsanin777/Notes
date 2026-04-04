package com.example.note;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.time.Instant;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Note implements Serializable {
    private static final String ARCIVE_NAME = "Notes.zip";
    private static final ZoneId deviceZone = ZoneId.systemDefault();
    private String m_hash_parent;
    private String m_hash;
    private String m_name;
    private String m_context;
    private Instant m_time;
    private boolean m_is_edited;
    private boolean m_is_deleted;
    private boolean m_is_rename;

    public Note(String name, String context) {
        m_name = name;
        m_context = context;
        m_time = now();
        m_is_edited = false;
        m_is_deleted = false;
        m_is_rename = false;
        serialize();
    }
    private Note(String name, String context,
                 Instant time, boolean is_edited,
                 boolean is_deleted, boolean is_rename) {
        m_name = name; m_context = context;
        m_time = time; m_is_edited = is_edited;
        m_is_deleted = is_deleted; m_is_rename = is_rename;
    }
    Note parce() {
        return new Note("jk", "kl");
    }
    public String getLabel() {
        return m_context;
    }
    public String getText() {
        return m_context;
    }
    public ZonedDateTime getTime() {
        return m_time.atZone(deviceZone);
    }
    public boolean isRename() {
        return m_is_rename;
    }
    public boolean isEdited() {
        return m_is_edited;
    }
    public boolean isDeleted() {
        return m_is_deleted;
    }
    public void edit(String newContext) {
        m_is_edited = true;
        m_time = now();
        m_context = newContext;
        serialize();
    }
    public void rename(String name) {
        m_is_rename = true;
        m_time = now();
        m_name = name;
        serialize();
    }

    public void delete() {
        m_is_deleted = true;
        serialize();
    }

    public void deleteHistory() {

    }

    public void deleteWhitHistory() {

    }

    private Instant now() {
        return Instant.now();
    }
}

