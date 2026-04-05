package com.example.note;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

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
import java.util.Date;
import java.time.Instant;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Note implements Parcelable {
    public static final Parcelable.Creator<Note> CREATOR = new Parcelable.Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };
    private static final ZoneId deviceZone = ZoneId.systemDefault();
    private static Context m_context;
    private String m_parentHash;
    private String m_currentHash;
    private String m_childHash;
    private Note m_parent;
    private Note m_child;
    private String m_name;
    private String m_content;
    private Instant m_time;
    private boolean m_isRenamed;
    private boolean m_isRenamedHistory;
    private boolean m_isEdited;
    private boolean m_isEditedHistory;
    private boolean m_isDeleted;

    public Note(String name, String content) {
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
    }

    public Note(String name, String content, Note parent) {
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
        updateHash();
        parent.m_childHash = m_currentHash;
        serialize();
        parent.serialize();
    }

    protected Note(Parcel in) {
        m_parentHash = in.readString();
        //m_hash = in.readString();
        m_childHash = in.readString();
        m_name = in.readString();
        m_content = in.readString();
        long millis = in.readLong();
        m_time = (millis != -1) ? Instant.ofEpochMilli(millis) : null;
        m_isRenamed = in.readByte() != 0;
        m_isRenamedHistory = in.readByte() != 0;
        m_isEdited = in.readByte() != 0;
        m_isEditedHistory = in.readByte() != 0;
        m_isDeleted = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(m_parentHash);
        //dest.writeString(m_hash);
        dest.writeString(m_childHash);
        dest.writeString(m_name);
        dest.writeString(m_content);
        dest.writeLong(m_time != null ? m_time.toEpochMilli() : -1);
        dest.writeByte((byte) (m_isRenamed ? 1 : 0));
        dest.writeByte((byte) (m_isRenamedHistory ? 1 : 0));
        dest.writeByte((byte) (m_isEdited ? 1 : 0));
        dest.writeByte((byte) (m_isEditedHistory ? 1 : 0));
        dest.writeByte((byte) (m_isDeleted ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private Note(String hashParent, String hash, String hashChild,
                 Note parent, Note child, String name,
                 String content, Instant time, boolean isRenamed,
                 boolean isRenamedHistory, boolean isEdited,
                 boolean isEditedHistory, boolean isDeleted) {
        m_parentHash = hashParent; m_currentHash = hash;
        m_childHash = hashChild; m_parent = parent;
        m_child = child; m_name = name; m_content = content;
        m_time = time; m_isRenamed = isRenamed;
        m_isRenamedHistory = isRenamedHistory;
        m_isEdited = isEdited; m_isEditedHistory = isEditedHistory;
        m_isDeleted = isDeleted;
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
        if (hash == null) {
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
        if (m_context == null || m_currentHash == null) return;

        Parcel parcel = Parcel.obtain();

        writeToParcel(parcel, 0);

        byte[] bytes = parcel.marshall();

        parcel.recycle();

        try (FileOutputStream fos = m_context.openFileOutput(m_currentHash, Context.MODE_PRIVATE)) {
            fos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Note deserialize(String fileName) {
        try (FileInputStream fis = m_context.openFileInput(fileName)) {
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);

            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(bytes, 0, bytes.length);

            parcel.setDataPosition(0);

            Note note = Note.CREATOR.createFromParcel(parcel);

            parcel.recycle();

            return note;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getName() {
        return m_name;
    }
    public String getContent() {
        return m_content;
    }
    public ZonedDateTime getTime() {
        return m_time.atZone(deviceZone);
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

    public void deleteForce(Context context) {

    }
    private static Instant now() {
        return Instant.now();
    }
    public static void setContext(Context context) {
        m_context = context;
    }
}

