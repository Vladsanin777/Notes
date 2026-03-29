package com.example.note;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.time.Instant;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Note {
    String m_label;
    String m_text;
    Instant m_time;
    boolean m_is_edited;
    boolean m_is_deleted;
    boolean m_is_rename;
    int m_id;
    static int id_note_last;
    static ZoneId deviceZone = ZoneId.systemDefault();
    static void set_id(int id_last) {
        id_note_last = id_last;
    }
    public Note(String label, String text) {
        m_id = ++id_note_last;
        m_label = label;
        m_text = text;
        m_time = now();
        m_is_edited = false;
        m_is_deleted = false;
        m_is_rename = false;
    }
    private Note(int id, String label, String text,
                 Instant time, boolean is_edited,
                 boolean is_deleted, boolean is_rename) {
        m_id = id; m_label = label; m_text = text;
        m_time = time; m_is_edited = is_edited;
        m_is_deleted = is_deleted; m_is_rename = is_rename;
    }
    Note parce() {
        return new Note("jk", "kl");
    }
    public String getLabel() {
        return m_label;
    }
    public String getText() {
        return m_text;
    }
    public int getIdNote() {
        return m_id;
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
    public void setText(String text) {
        m_is_edited = true;
        m_time = now();
        m_text = text;
    }
    public void setLabel(String label) {
        m_is_rename = true;
        m_label = label;
    }
    public void delete() {
        m_is_deleted = true;
    }
    public void deleteForce() {

    }
    private Instant now() {
        return Instant.now();
    }
}

