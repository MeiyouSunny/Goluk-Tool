/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rd.exoplayer2.decoder;

import android.annotation.TargetApi;
import com.rd.exoplayer2.C;
import com.rd.exoplayer2.util.Util;

/**
 * Compatibility wrapper for {@link android.media.MediaCodec.CryptoInfo}.
 */
public final class CryptoInfo {

  /**
   * @see android.media.MediaCodec.CryptoInfo#iv
   */
  public byte[] iv;
  /**
   * @see android.media.MediaCodec.CryptoInfo#key
   */
  public byte[] key;
  /**
   * @see android.media.MediaCodec.CryptoInfo#mode
   */
  @C.CryptoMode
  public int mode;
  /**
   * @see android.media.MediaCodec.CryptoInfo#numBytesOfClearData
   */
  public int[] numBytesOfClearData;
  /**
   * @see android.media.MediaCodec.CryptoInfo#numBytesOfEncryptedData
   */
  public int[] numBytesOfEncryptedData;
  /**
   * @see android.media.MediaCodec.CryptoInfo#numSubSamples
   */
  public int numSubSamples;

  private final android.media.MediaCodec.CryptoInfo frameworkCryptoInfo;

  public CryptoInfo() {
    frameworkCryptoInfo = Util.SDK_INT >= 16 ? newFrameworkCryptoInfoV16() : null;
  }

  /**
   * @see android.media.MediaCodec.CryptoInfo#set(int, int[], int[], byte[], byte[], int)
   */
  public void set(int numSubSamples, int[] numBytesOfClearData, int[] numBytesOfEncryptedData,
      byte[] key, byte[] iv, @C.CryptoMode int mode) {
    this.numSubSamples = numSubSamples;
    this.numBytesOfClearData = numBytesOfClearData;
    this.numBytesOfEncryptedData = numBytesOfEncryptedData;
    this.key = key;
    this.iv = iv;
    this.mode = mode;
    if (Util.SDK_INT >= 16) {
      updateFrameworkCryptoInfoV16();
    }
  }

  /**
   * Returns an equivalent {@link android.media.MediaCodec.CryptoInfo} instance.
   * <p>
   * Successive calls to this method on a single {@link CryptoInfo} will return the same instance.
   * Changes to the {@link CryptoInfo} will be reflected in the returned object. The return object
   * should not be modified directly.
   *
   * @return The equivalent {@link android.media.MediaCodec.CryptoInfo} instance.
   */
  @TargetApi(16)
  public android.media.MediaCodec.CryptoInfo getFrameworkCryptoInfoV16() {
    return frameworkCryptoInfo;
  }

  @TargetApi(16)
  private android.media.MediaCodec.CryptoInfo newFrameworkCryptoInfoV16() {
    return new android.media.MediaCodec.CryptoInfo();
  }

  @TargetApi(16)
  private void updateFrameworkCryptoInfoV16() {
    frameworkCryptoInfo.set(numSubSamples, numBytesOfClearData, numBytesOfEncryptedData, key, iv,
        mode);
  }

}
