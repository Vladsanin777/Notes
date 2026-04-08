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

@RequiresApi(api = Build.VERSION_CODES.O)
public class Note {
    private static final String HEADS = "HEADS_NOTE";
    private static final String PREFIX = "note:";
    private final static ArrayList<Note> m_notes = new ArrayList<Note>();
    private final static ArrayList<Note> m_notesTemplate = new ArrayList<Note>();
    private final static ArrayList<Note> m_notesDeleted = new ArrayList<Note>();
    private static final ZoneId m_deviceZone = ZoneId.systemDefault();
    private static Context m_context;
    private int m_indexNote;
    private String m_parentHash;
    private String m_currentHash;
    private Set<String> m_childsHashs;
    private Note m_parent;
    private final Set<Note> m_childs = new HashSet<Note>();
    private String m_name;
    private String m_content;
    private Instant m_time;
    private boolean m_isRenamed;
    private boolean m_isRenamedHistory;
    private boolean m_isEdited;
    private boolean m_isEditedHistory;
    private boolean m_isDeleted;
    private boolean m_isTemplate;

    public static void INIT() {
        SharedPreferences prefs = m_context.getSharedPreferences(HEADS, Context.MODE_PRIVATE);

        Set<String> savedHeads = prefs.getStringSet(HEADS, null);

        if (savedHeads != null) {
            for (String head : savedHeads) {
                Log.d("hash", head);
                deserialize(head);
            }
        }
    }

    private static void updateHeads() {
        Log.d("debug", "updateHeads вызвана. Размер m_notes: " + m_notes.size());

        SharedPreferences prefs = m_context.getSharedPreferences(HEADS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> headsToSave = new HashSet<>();

        for (int i = 0; i < m_notes.size(); i++) {
            Log.d("head", PREFIX + m_notes.get(i).m_currentHash);
            headsToSave.add(PREFIX + m_notes.get(i).m_currentHash);
        }

        editor.putStringSet(HEADS, headsToSave);
        editor.apply();
    }

    public Note(String name, String content) {
        Log.d("debug", "updateHeads вызвана. Размер m_notes: " + m_notes.size());
        m_name = name;
        m_content = content;
        m_time = now();
        m_isRenamed = false;
        m_isRenamedHistory = false;
        m_isEdited = false;
        m_isEditedHistory = false;
        m_isDeleted = false;
        updateHash();
        serialize();
        m_indexNote = m_notes.size();
        m_notes.add(this);
        updateHeads();
    }

    public Note(String name, String content, Note parent, Boolean template) {
        m_parentHash = parent.m_currentHash;
        m_parent = parent;
        m_name = name;
        m_content = content;
        m_time = now();
        m_isRenamed = isRenamed(parent);
        m_isRenamedHistory = parent.isRenamed();
        m_isEdited = isEdited(parent);
        m_isEditedHistory = parent.isEdited();
        m_isDeleted = false;
        m_isTemplate = template;
        updateHash();
        serialize();
        parent.serialize();
        if (parent == null) {
            m_indexNote = m_notes.size();
            m_notes.add(this);
        } else {
            m_indexNote = parent.m_indexNote;
            m_notes.set(parent.m_indexNote, this);
            if (parent.m_childsHashs.size() != 0)
                parent.m_isTemplate = true;
            parent.m_childsHashs.add(m_currentHash);
            parent.m_childs.add(this);
        }
        updateHeads();
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
        m_parentHash = in.getString("parentHash", null);
        m_childsHashs = in.getStringSet("childsHashs", m_childsHashs);
        m_name = in.getString("name", null);
        m_content = in.getString("content", null);
        long millis = in.getLong("time", -1);
        m_time = (millis != -1) ? Instant.ofEpochMilli(millis) : null;
        m_isRenamed = in.getBoolean("renamed", false);
        m_isRenamedHistory = in.getBoolean("renameHistory", false);
        m_isEdited = in.getBoolean("edited", false);
        m_isEditedHistory = in.getBoolean("editedHistory", false);
        m_isDeleted = in.getBoolean("deleted", false);
        m_isTemplate = in.getBoolean("template", false);
        updateHash();
    }

    private void setSinglton() {
        if (isDeleted()) {
            m_indexNote = m_notesDeleted.size();
            m_notesDeleted.add(this);
        } else if (isTemplate()) {
            m_indexNote = m_notesTemplate.size();
            m_notesTemplate.add(this);
        } else {
            m_indexNote = m_notes.size();
            m_notes.add(this);
        }
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

        editor.putString("parentHash", m_parentHash);
        editor.putStringSet("childsHashs", m_childsHashs);
        editor.putString("name", m_name);
        editor.putString("content", m_content);
        editor.putLong("time", m_time != null ? m_time.toEpochMilli() : -1);
        editor.putBoolean("renamed", m_isRenamed);
        editor.putBoolean("renamedHistory", m_isRenamedHistory);
        editor.putBoolean("edited", m_isEdited);
        editor.putBoolean("editedHistory", m_isEditedHistory);
        editor.putBoolean("deleted", m_isDeleted);
        editor.putBoolean("template", m_isTemplate);

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

