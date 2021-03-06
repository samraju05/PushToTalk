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

package com.morlunk.jumble.audio;

import com.morlunk.jumble.exception.NativeAudioException;

/**
 * Created by andrew on 07/03/14.
 */
public interface IEncoder {
    /**
     * Encodes the provided input and returns the number of bytes encoded.
     * @param input The short PCM data to encode.
     * @param inputSize The number of samples to encode.
     * @param output The output buffer.
     * @param outputSize The size of the output buffer.
     * @return The number of bytes encoded.
     * @throws NativeAudioException if there was an error encoding.
     */
    public int encode(short[] input, int inputSize, byte[] output, int outputSize) throws NativeAudioException;
    public void setBitrate(int bitrate);
    public void destroy();
}
