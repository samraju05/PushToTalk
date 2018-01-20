/*
 * Copyright (C) 2014 Andrew Comminos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.morlunk.jumble.util;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by andrew on 05/04/14.
 */
public class ParcelableByteArray implements Parcelable {

    private byte[] mByteArray;

    public ParcelableByteArray(byte[] array) {
        mByteArray = array;
    }

    private ParcelableByteArray(Parcel in) {
        int length = in.readInt();
        mByteArray = new byte[length];
        in.readByteArray(mByteArray);
    }

    public byte[] getBytes() {
        return mByteArray;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mByteArray.length);
        dest.writeByteArray(mByteArray);
    }

    public static final Creator<ParcelableByteArray> CREATOR = new Creator<ParcelableByteArray>() {

        @Override
        public ParcelableByteArray createFromParcel(Parcel source) {
            return new ParcelableByteArray(source);
        }

        @Override
        public ParcelableByteArray[] newArray(int size) {
            return new ParcelableByteArray[size];
        }
    };
}
