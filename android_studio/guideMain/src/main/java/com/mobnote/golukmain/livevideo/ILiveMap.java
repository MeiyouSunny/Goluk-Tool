package com.mobnote.golukmain.livevideo;

import android.os.Bundle;

/**
 * Created by leege100 on 16/6/25.
 */
public interface ILiveMap {

    public void initMap(Bundle bundle);

    public void updatePublisherMarker(double lat,double lon);

    public void updateAudienceMarker(double lat,double lon);

    public void toMyLocation();
}
