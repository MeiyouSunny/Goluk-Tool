package com.mobnote.videoedit.bean;

import cn.npnt.ae.model.Chunk;

public class ChunkBean extends ProjectItemBean {
    public Chunk chunk;
    public int width;
    public boolean isEditState;

    // Tag to match chunk and transition
    public String ct_pair_tag;
    public int curVolume;
    public boolean isMute;
}
