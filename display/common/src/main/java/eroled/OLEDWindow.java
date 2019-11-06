/**
 * Copyright © 2016-2019 Lightbend, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * NO COMMERCIAL SUPPORT OR ANY OTHER FORM OF SUPPORT IS OFFERED ON
 * THIS SOFTWARE BY LIGHTBEND, Inc.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eroled;

import java.io.IOException;
import java.util.Arrays;

import static java.lang.Math.abs;
import static java.lang.Math.min;

public class OLEDWindow {
    BasicOLED oled;
    int x;
    int y;
    int width;
    int height;
    byte[] buffer;
    byte _0F= 0x0F;
    public OLEDWindow(BasicOLED oled,
                      int x,
                      int y,
                      int width,
                      int height){
        this.oled = oled;
        this.x=x;
        this.y=y;
        this.width=width;
        this.height=height;
        this.buffer=new byte[(width/2)*height];

    }
    public void drawScreenBuffer() throws IOException {
        oled.setDataWindow(x,y,width,height);
        oled.writeInstruction(0x5c);
        for ( int i=0;i<buffer.length;i+=2048)
        {
            oled.writeDataBytes(Arrays.copyOfRange(buffer, i, min(i+2048,buffer.length)));
        }
    }

    public void setPixel(int x, int y, byte color){
        color &= 0x0F;
        int ofs = y * width/2;
        ofs = ofs + x/2;// 2 pixels per byte across row
        byte v;
        if (x%2 == 0) {
            v = (byte)(buffer[ofs] & 0x0F);
            v |= (color << 4);
        } else {
            v = (byte)(buffer[ofs] & 0xF0);
            v |= color;
        }
        buffer[ofs] = v;
    }

    public void drawBwImage(int x,int y,int width,int height,byte color,byte[] data,int offs){
        int pos = offs;
        for(int yy = 0; yy<height; yy++){
            int ox = x;
            for(int xx = 0; xx<width/8; xx++){
             int  mask = 0b10000000;
                for(int pp = 0; pp<8; pp++){
                    if((data[pos] & mask)>0)
                        setPixel(x,y,color);
                    else
                        setPixel(x,y,(byte)0);
                    mask = mask>>1;
                    x=x + 1;
                }
                pos++;
            }
            x=ox;
            y = y + 1;
        }
    }

    public void drawBigBwImage(int x, int y, int width, int height, byte color, byte[] data, int offs){
        int pos = offs;
        byte black = 0;
        for(int yy = 0; yy<height; yy++) {
            int ox = x;
            for (int xx = 0; xx < width / 8; xx++) {
                int mask = 128;
                for (int pp = 0; pp < 8; pp++) {
                    if((data[pos] & mask)>0) {
                        setPixel(x, y, color);
                        setPixel(x + 1, y, color);
                        setPixel(x, y + 1, color);
                        setPixel(x + 1, y + 1, color);
                    } else{
                        setPixel(x, y, black);
                        setPixel(x + 1, y, black);
                        setPixel(x, y + 1, black);
                        setPixel(x + 1, y + 1, black);
                    }
                    mask = mask>>1;
                    x=x + 2;
                }
                pos++;
            }
            x=ox;
            y = y + 1;
        }
    }
    private void swap(int a, int b)
    {
        int temp = a;
        a = b;
        b = temp;

    }

    public void drawLine(int startx, int starty,
                         int endx, int endy,
                         byte color){

        int dx = endx - startx;
        int dy = endy - starty;

        //# Determine how steep the line is
        boolean is_steep = abs(dy) > abs(dx);


        if (is_steep){
            int temp = startx;
            startx = starty;
            starty = temp;

            temp = endx;
            endx = endy;
            endy = temp;


        }


        if (startx > endx){
            int temp = startx;
            startx = endx;
            endx = temp;

            temp = starty;
            startx = starty;
            starty = temp;
        }

        dx = endx - startx;
        dy = endy - starty;


        int error = (int)(dx / 2.0);
        int ystep = (starty < endy) ? 1 : -1;

        // Iterate over bounding box generating points between start and end
        int y = starty;
        int[] points =null;
        for(int x = startx;x<endx+1;x++){
            if (is_steep) setPixel(y,x,color); else setPixel(x,y,color);
            error -= abs(dy);
            error -= abs(dy);
            if (error < 0) {
                y += ystep;
                error += dx;
            }
        }
    }

    public void drawRectangle(int x, int y, int width, int height, byte color){
        for(int xx=0;xx<width;xx++){
            setPixel(x+xx,y,color);
            setPixel(x+xx,y+height-1, color);
        }
        for(int yy=0;x<height;yy++){
            setPixel(x,y+yy,color);
            setPixel(x+width-1,y+yy,color);
        }
    }

    public void drawVLine(int x, int y, int h, byte color){
        drawLine(x, y, x, y+h-1, color);
    }
    public void drawHLine(int x, int y, int w, byte color){
        drawLine(x, y, x+w-1, y, color);
    }

    public void drawFrame(int x,int y,int w, int h,byte color){
        drawHLine(x, y, w, color);
        drawHLine(x, y+h-1, w, color);
        drawVLine(x, y, h, color);
        drawVLine(x+w-1, y, h, color);
    }

}
