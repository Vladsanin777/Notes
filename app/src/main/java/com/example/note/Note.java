package com.example.note;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.example.note.TypeNote.*;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Note {
    private static final String HEADS_HOTES = "HEADS_NOTE";
    private static final String DELETEDS_HOTES = "DELETED_NOTE";
    private static final String TEMPLATES_HOTES = "TEMPLATE_NOTE";
    private static final String HEAD_FILE = "HEAD";
    private static final String PREFIX = "note:";
    private static final ArrayList<Note> m_headsNotes = new ArrayList<Note>();
    private static final ArrayList<Note> m_templateNotes = new ArrayList<Note>();
    private static final ArrayList<Note> m_deletedNotes = new ArrayList<Note>();
    private static final ZoneId m_deviceZone = ZoneId.systemDefault();
    private static final Map<String, Note> m_allNotes = new HashMap<String, Note>();
    private static Context m_context;
    private int m_indexNote;
    private String m_parentHash;
    private String m_hash;
    private Note m_parent;
    private String m_name;
    private String m_content;
    private Instant m_time;
    private boolean m_isRenamed;
    private boolean m_isEdited;
    private TypeNote m_type;

    public static void INIT_ALL(Context context) {
        setContext(context);

        String[] files = m_context.fileList();

        for (String file : files) {
            m_allNotes.replace(file.substring(5), deserialize(file));
        }

        INIT_HEADS_NOTES();
        INIT_TEMPLATE_HOTES();
        INIT_DELEED_HOTES();

        INIT_PARENT_HEADS_NOTES();
        INIT_PARENT_TEMPLATE_HOTES();
        INIT_PARENT_DELEED_HOTES();
    }

    public static void INIT_DELEED_HOTES() {
        INIT_BASE(HEAD_FILE, DELETEDS_HOTES, m_deletedNotes);
    }

    public static void INIT_TEMPLATE_HOTES() {
        INIT_BASE(HEAD_FILE, TEMPLATES_HOTES, m_templateNotes);
    }

    public static void INIT_HEADS_NOTES() {
        INIT_BASE(HEAD_FILE, HEADS_HOTES, m_headsNotes);
    }

    public static void INIT_BASE(String fileName, String key, ArrayList<Note> list) {
        SharedPreferences prefs = m_context.getSharedPreferences(fileName, Context.MODE_PRIVATE);

        Set<String> hashs = prefs.getStringSet(key, null);

        if (hashs != null) {
            for (String hesh : hashs) {
                Log.d("hash", hesh);
                list.add(m_allNotes.get(hesh));
            }
        }
    }

    public static void INIT_PARENT_DELEED_HOTES() {
        INIT_PARENT_BASE(m_deletedNotes);
    }

    public static void INIT_PARENT_TEMPLATE_HOTES() {
        INIT_PARENT_BASE(m_templateNotes);
    }

    public static void INIT_PARENT_HEADS_NOTES() {
        INIT_PARENT_BASE(m_headsNotes);
    }
    public static void INIT_PARENT_BASE(ArrayList<Note> list) {
        for (Note child : list) {
            while (child.m_parentHash != null) {
                child.m_parent = m_allNotes.get(child.m_parentHash);
                child = child.m_parent;
            }
        }
    }

    private static void UPDATE_HEADS() {
        UPDATE_BASE(HEAD_FILE, HEADS_HOTES, m_headsNotes);
    }

    private static void UPDATE_TEMPLATE() {
        UPDATE_BASE(HEAD_FILE, TEMPLATES_HOTES, m_templateNotes);
    }

    private static void UPDATE_DELETED() {
        UPDATE_BASE(HEAD_FILE, DELETEDS_HOTES, m_deletedNotes);
    }

    private static void UPDATE_BASE(String fileName, String key, ArrayList<Note> list) {
        SharedPreferences prefs = m_context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> headsToSave = new HashSet<>();

        for (int i = 0; i < m_headsNotes.size(); i++) {
            headsToSave.add(PREFIX + m_headsNotes.get(i).m_hash);
        }

        editor.putStringSet(key, headsToSave);
        editor.apply();
    }

    public Note(String name, String content, TypeNote type) {
        m_name = name;
        m_content = content;
        m_time = now();
        m_isRenamed = false;
        m_isEdited = false;
        m_type = type;
        updateHash();
        serialize();
    }

    public Note(String name, String content, TypeNote type, Note parent) {
        m_parentHash = parent.m_hash;
        m_parent = parent;
        m_name = name;
        m_content = content;
        m_time = now();
        m_isRenamed = isRenamed(parent);
        m_isEdited = isEdited(parent);
        m_type = type;
        updateHash();
        serialize();
    }

    protected Note(SharedPreferences in) {
        parce(in);
    }

    private void parce(SharedPreferences in) {
        m_parentHash = in.getString("parentHash", null);
        m_name = in.getString("name", null);
        m_content = in.getString("content", null);
        long millis = in.getLong("time", -1);
        m_time = (millis != -1) ? Instant.ofEpochMilli(millis) : null;
        m_isRenamed = in.getBoolean("renamed", false);
        m_isEdited = in.getBoolean("edited", false);
        m_type = TypeNote.values()[in.getInt("type", TypeNote.NOTE.ordinal())];
        updateHash();
    }

    private void unparce(SharedPreferences out) {
        SharedPreferences.Editor editor = out.edit();

        editor.putString("parentHash", m_parentHash);
        editor.putString("name", m_name);
        editor.putString("content", m_content);
        editor.putLong("time", m_time != null ? m_time.toEpochMilli() : -1);
        editor.putBoolean("renamed", m_isRenamed);
        editor.putBoolean("edited", m_isEdited);
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
            m_hash = hash;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void serialize() {
        if (m_hash == null) return;

        SharedPreferences prefs = m_context.getSharedPreferences(PREFIX + m_hash, Context.MODE_PRIVATE);

        unparce(prefs);
    }

    public static Note deserialize(String fileName) {
        if (!fileName.startsWith(PREFIX))
            return null;
        SharedPreferences prefs = m_context.getSharedPreferences(fileName, Context.MODE_PRIVATE);

        Note note = new Note(prefs);

        return note;
    }

    public static int getHeadCount() {
        return m_headsNotes.size();
    }

    public static int getTemplateCount() {
        return m_headsNotes.size();
    }
    public static int getDeletedCount() {
        return m_headsNotes.size();
    }

    public static Note getHeadNote(int i) {
        return m_headsNotes.get(i);
    }

    public static Note getTemplateNote(int i) {
        return m_templateNotes.get(i);
    }

    public static Note getDeletedNote(int i) {
        return m_deletedNotes.get(i);
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

    public boolean isRenamed() {
        return m_isRenamed;
    }

    public boolean isRenamed(String newName) {
        return !m_name.equals(newName);
    }

    public boolean isRenamed(Note note) {
        return !m_name.equals(note.m_name);
    }

    public boolean isEdited() {
        return m_isEdited;
    }

    public boolean isEdited(String newContent) {
        return !m_content.equals(newContent);
    }

    public boolean isEdited(Note note) {
        return !m_content.equals(note.m_content);
    }


    public boolean isDeleted() {
        return m_type == TypeNote.DELETED;
    }

    public void template() {
        switch (m_type) {
            case NOTE:
                if (m_headsNotes.get(m_indexNote) == this) {
                    m_headsNotes.set(m_indexNote, null);
                }
                m_indexNote = m_templateNotes.size();
                m_templateNotes.add(this);
                m_type = TypeNote.TEMPLATE;
                serialize();
                break;
            case DELETED:
                if (m_deletedNotes.get(m_indexNote) == this) {
                    m_deletedNotes.set(m_indexNote, null);
                }
                m_indexNote = m_templateNotes.size();
                m_templateNotes.add(this);
                m_type = TypeNote.TEMPLATE;
                serialize();
                break;
        }

        serialize();
    }

    public void delete() {
        switch (m_type) {
            case NOTE:
                if (m_headsNotes.get(m_indexNote) == this) {
                    m_headsNotes.set(m_indexNote, null);
                }
                m_indexNote = m_deletedNotes.size();
                m_deletedNotes.add(this);
                m_type = TypeNote.DELETED;
                serialize();
                break;
            case TEMPLATE:
                if (m_templateNotes.get(m_indexNote) == this) {
                    m_templateNotes.set(m_indexNote, null);
                }
                m_indexNote = m_deletedNotes.size();
                m_deletedNotes.add(this);
                m_type = TypeNote.DELETED;
                serialize();
                break;
        }
    }


    public void deleteForce() {
        switch (m_type) {
            case NOTE:
                if (m_headsNotes.get(m_indexNote) == this) {
                    m_headsNotes.set(m_indexNote, null);
                }
                break;
            case DELETED:
                if (m_deletedNotes.get(m_indexNote) == this) {
                    m_deletedNotes.set(m_indexNote, null);
                }
                break;
            case TEMPLATE:
                if (m_templateNotes.get(m_indexNote) == this) {
                    m_templateNotes.set(m_indexNote, null);
                }
                break;
        }

        File note = new File(m_context.getFilesDir(), PREFIX + m_hash);

        if (note.exists()) {
            note.delete();
        }
    }

    public Boolean isTemplate() {
        return m_type == TypeNote.TEMPLATE;
    }

    public Note getParent() {
        return m_parent;
    }

    private static Instant now() {
        return Instant.now();
    }

    private static void setContext(Context context) {
        m_context = context;
    }
}