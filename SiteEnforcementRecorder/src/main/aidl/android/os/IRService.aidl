/*
** Copyright 2017 Trutalk WBY
*/

package android.os;

/** {@hide} */
interface IRService
{
    void IRLEDBrightness(int brightness);
    void IRCUTForward();
    void IRCUTReverse();
    float IRLightSensor();
    void ScreenOn();
    void ScreenOff();

    void BLNCTRLBrightness(int color,int brightness);
    void BLNCTRLBlink(int color,int brightness);
}
