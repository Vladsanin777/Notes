package com.example.note;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.BoringLayout;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.time.Instant;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.example.note.TypeNote.*;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Note {
    private static final String HEADS_HOTES = "HEADS_NOTE";
    private static final String DELETEDS_HOTES = "DELETED_NOTE";
    private static final String TEMPLATES_HOTES = "TEMPLATE_NOTE";
    private static final String HEAD_FILE = "HEAD";
    private static final String PREFIX = "note:";
    private final static ArrayList<Note> m_headsNotes = new ArrayList<Note>();
    private final static ArrayList<Note> m_templateNotes = new ArrayList<Note>();
    private final static ArrayList<Note> m_deletedNotes = new ArrayList<Note>();
    private static final ZoneId m_deviceZone = ZoneId.systemDefault();
    private static Context m_context;
    private int m_indexNote;
    private String m_hash;
    private Note m_parent;
    private String m_name;
    private String m_content;
    private Instant m_time;
    private boolean m_isRenamed;
    private boolean m_isRenamedHistory;
    private boolean m_isEdited;
    private boolean m_isEditedHistory;
    private TypeNote m_type;

    public static void INIT_ALL() {
        INIT_HEADS_NOTES();
        INIT_TEMPLATE_HOTES();
        INIT_DELEED_HOTES();
    }

    public static void INIT_DELEED_HOTES() {
        INIT_BASE(DELETEDS_HOTES, m_deletedNotes);
    }

    public static void INIT_TEMPLATE_HOTES() {
        INIT_BASE(TEMPLATES_HOTES, m_templateNotes);
    }

    public static void INIT_HEADS_NOTES() {
        INIT_BASE(HEADS_HOTES, m_headsNotes);
    }

    public static void INIT_BASE(String key, ArrayList<Note> list) {
        SharedPreferences prefs = m_context.getSharedPreferences(HEADS_FILE, Context.MODE_PRIVATE);

        Set<String> saved = prefs.getStringSet(HEADS_FILE, null);

        if (saved != null) {
            for (String head : saved) {
                Log.d("hash", head);
                list.add(deserialize(head));
            }
        }
    }

    private static void updateHeadsNotes() {
        Log.d("debug", "updateHeads вызвана. Размер m_notes: " + m_headsNotes.size());

        SharedPreferences prefs = m_context.getSharedPreferences(HEADS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> headsToSave = new HashSet<>();

        for (int i = 0; i < m_headsNotes.size(); i++) {
            Log.d("head", PREFIX + m_headsNotes.get(i).m_currentHash);
            headsToSave.add(PREFIX + m_headsNotes.get(i).m_currentHash);
        }

        editor.putStringSet(HEADS_FILE, headsToSave);
        editor.apply();
    }

    public Note(String name, String content, TypeNote type) {
        m_name = name;
        m_content = content;
        m_time = now();
        m_isRenamed = false;
        m_isRenamedHistory = false;
        m_isEdited = false;
        m_isEditedHistory = false;
        m_type = type;
        updateHash();
        serialize();
        setSingleton();
    }

    public Note(String name, String content, TypeNote type, Note parent) {
        m_parent = parent;
        m_name = name;
        m_content = content;
        m_time = now();
        m_isRenamed = isRenamed(parent);
        m_isRenamedHistory = parent.isRenamed();
        m_isEdited = isEdited(parent);
        m_isEditedHistory = parent.isEdited();
        m_type = type;
        updateHash();
        serialize();

        setSingleton();
    }

    protected Note(SharedPreferences in, Note child) {
        parce(in);

        if (child == null) {
            if (isDeleted()) {
                m_indexNote = m_notesDeleted.size();
                m_notesDeleted.add(this);
            } else {
                m_indexNote = m_notes.size();
                m_notes.add(this);
            }
        } else {
            child.m_parent = this;
            m_indexNote = child.m_indexNote;
        }
        if (m_parentHash != null)
            deserialize(PREFIX + m_parentHash, this);
    }

    protected Note(SharedPreferences in) {
        parce(in);

        setSinglton();

        if (m_parentHash != null)
            deserialize(PREFIX + m_parentHash, this);
    }

    private void setSingleton() {
        switch (m_type) {
            case NOTE:
                if (m_parent != null && m_parent.m_type == TypeNote.NOTE) {
                    m_indexNote = m_parent.m_indexNote;
                    if (m_headsNotes.get(m_indexNote) != m_parent) {
                        m_parent.setTemplate();
                    }
                    m_headsNotes.set(m_indexNote, this);
                } else {
                    m_indexNote = m_headsNotes.size();
                    m_headsNotes.add(this);
                }
                break;
            case TEMPLATE:
                m_indexNote = m_templateNotes.size();
                m_templateNotes.add(this);
                break;
            case DELETED:
                m_indexNote = m_deletedNotes.size();
                m_deletedNotes.add(this);
                break;
        }
    }

    public void setTemplate() {
        switch (m_type) {
            case NOTE:
                if (m_headsNotes.get(m_indexNote) == this) {
                    m_headsNotes.remove();
                }
                break;
        }
        m_type = TypeNote.TEMPLATE;

        serialize();
    }

    private Note(String hashParent, String hash, String hashChild,
                 Note parent, Note child, String name,
                 String content, Instant time, boolean isRenamed,
                 boolean isRenamedHistory, boolean isEdited,
                 boolean isEditedHistory, boolean isDeleted) {
        m_parentHash = hashParent; m_currentHash = hash;
        m_name = name; m_content = content;
        m_time = time; m_isRenamed = isRenamed;
        m_isRenamedHistory = isRenamedHistory;
        m_isEdited = isEdited; m_isEditedHistory = isEditedHistory;
        m_isDeleted = isDeleted;
    }

    private void parce(SharedPreferences in) {
        m_name = in.getString("name", null);
        m_content = in.getString("content", null);
        long millis = in.getLong("time", -1);
        m_time = (millis != -1) ? Instant.ofEpochMilli(millis) : null;
        m_isRenamed = in.getBoolean("renamed", false);
        m_isRenamedHistory = in.getBoolean("renameHistory", false);
        m_isEdited = in.getBoolean("edited", false);
        m_isEditedHistory = in.getBoolean("editedHistory", false);
        m_type = TypeNote.values()[in.getInt("type", TypeNote.NOTE.ordinal())];
        updateHash();
    }

    private void unparce(SharedPreferences out) {
        SharedPreferences.Editor editor = out.edit();

        editor.putString("parentHash", m_parentHash);
        editor.putStringSet("childsHashs", m_childsHashs);
        editor.putString("name", m_name);
        editor.putString("content", m_content);
        editor.putLong("time", m_time != null ? m_time.toEpochMilli() : -1);
        editor.putBoolean("renamed", m_isRenamed);
        editor.putBoolean("renamedHistory", m_isRenamedHistory);
        editor.putBoolean("edited", m_isEdited);
        editor.putBoolean("editedHistory", m_isEditedHistory);
        editor.putInt("type", m_type.ordinal());

        editor.apply();
    }

    private String generateHash() {
        byte[] nameBytes = null;
        byte[] contentBytes = null;
        byte[] parentHashBytes = null;
        byte[] timeBytes = null;

        MessageDigest md = null;

        try {
            if (m_name != null)
                nameBytes = m_name.getBytes("UTF-8");
            if (m_content != null)
                contentBytes = m_content.getBytes("UTF-8");
            if (m_parentHash != null)
                parentHashBytes = m_parentHash.getBytes("UTF-8");
            if (m_time != null)
                timeBytes = String.valueOf(m_time.getEpochSecond()).getBytes(StandardCharsets.UTF_8);

            md = MessageDigest.getInstance("SHA-1");
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        if (nameBytes != null) md.update(nameBytes);
        if (contentBytes != null) md.update(contentBytes);
        if (parentHashBytes != null) md.update(parentHashBytes);
        if (timeBytes != null) md.update(timeBytes);

        byte[] hashBytes = md.digest();

        return bytesToHex(hashBytes);
    }

    private void updateHash() {
        String hash = generateHash();
        if (hash != null) {
            m_currentHash = hash;
        }
    }

    private String getHash() {
        return m_currentHash;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void serialize() {
        if (m_currentHash == null) return;

        SharedPreferences prefs = m_context.getSharedPreferences(PREFIX + m_currentHash, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();



        editor.apply();
    }

    public static Note deserialize(String fileName) {
        if (!fileName.startsWith(PREFIX))
            return null;
        SharedPreferences prefs = m_context.getSharedPreferences(fileName, Context.MODE_PRIVATE);

        Note note = new Note(prefs);

        return note;
    }

    private static Note deserialize(String fileName, Note child) {
        if (!fileName.startsWith(PREFIX))
            return null;
        SharedPreferences prefs = m_context.getSharedPreferences(HEADS, Context.MODE_PRIVATE);

        Note note = new Note(prefs, child);

        return note;
    }

    public static int getCount() {
        return m_notes.size();
    }

    public static Note getNote(int i) {
        return m_notes.get(i);
    }

    public static Note getNoteDeleted(int i) {
        return m_notesDeleted.get(i);
    }

    public int getId() {
        return m_indexNote;
    }

    public String getName() {
        return m_name;
    }
    public String getContent() {
        return m_content;
    }
    public ZonedDateTime getTime() {
        return m_time.atZone(m_deviceZone);
    }
    public boolean isRenamedCurrent() {
        return m_isRenamed;
    }

    public boolean isRenamedHistory() {
        return m_isRenamedHistory;
    }

    public boolean isRenamed() {
        return isRenamedCurrent() || isRenamedHistory();
    }

    public boolean isRenamed(String newName) {
        return !m_name.equals(newName);
    }

    public boolean isRenamed(Note note) {
        return !m_name.equals(note.m_name);
    }

    public boolean isEditedCurrent() {
        return m_isEdited;
    }

    public boolean isEditedHistory() {
        return m_isEditedHistory;
    }

    public boolean isEdited() {
        return isEditedCurrent() || isEditedHistory();
    }

    public boolean isEdited(String newContent) {
        return !m_content.equals(newContent);
    }

    public boolean isEdited(Note note) {
        return !m_content.equals(note.m_content);
    }


    public boolean isDeleted() {
        return m_isDeleted;
    }

    public void delete() {
        m_isDeleted = true;
        serialize();
    }

    public void deleteForce() {
        if (isDeleted()) {
            m_notesDeleted.set(m_indexNote, null);
        } else {
            m_notes.set(m_indexNote, null);
        }
        File file = new File(m_context.getFilesDir(), PREFIX + m_currentHash);
        if (file.exists()) {
            file.delete();
        }
    }

    public Boolean isTemplate() {
        return m_isTemplate;
    }

    public Note getParent() {
        return m_parent;
    }

    private static Instant now() {
        return Instant.now();
    }

    public static void setContext(Context context) {
        m_context = context;
    }
}

