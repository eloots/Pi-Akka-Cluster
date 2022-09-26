/**
 * Copyright © 2016-2019 Lightbend, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>
 * NO COMMERCIAL SUPPORT OR ANY OTHER FORM OF SUPPORT IS OFFERED ON
 * THIS SOFTWARE BY LIGHTBEND, Inc.
 * <p>
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eroled;


import java.io.IOException;

/**
 * Slow text rendering API
 */
public class TextCanvas {
    public TextCanvas(BasicOLEDDriver oledDriver, Font font) throws IOException, InterruptedException {
        super();
        this.font = font;
        this.oledDriver=oledDriver;
    }

    private static final String COLUMN_DELIMETER = " | ";
    private Font font;
    private BasicOLEDDriver oledDriver;
    public void setFont(Font font) {
        this.font = font;
    }

    public void drawSingleAscii(int x, int y, char c) throws IOException {
        int[] letter = font.getChar(c);
        for (int i = 0; i < 16; i++) {
            oledDriver.setRowAddress(y + i);
            oledDriver.setColumnAddress(x);
            oledDriver.writeInstruction(0x5c);
            dataProcessing(letter[i]);
        }
    }

    public void drawSpreadsheetInColumns(String[][] pStr) throws IOException {
        int lines = (pStr.length / font.getScreenHeight());
        lines += (pStr.length % font.getScreenHeight() > 0) ? 1 : 0;
        int columnSize = font.getScreenWidth() / lines;
        String[] result = new String[font.getScreenHeight()];
        for (int i = 0; i < pStr.length; i++) {
            String key = pStr[i][0];
            String val = pStr[i][1];
            String res = key + generateWhitespaces(columnSize - key.length() - val.length()) + val;
            String oldVal = result[i % font.getScreenHeight()];
            if (oldVal != null) oldVal += COLUMN_DELIMETER + res;
            else oldVal = res;
            result[i % font.getScreenHeight()] = oldVal;
        }
        StringBuffer multiline = new StringBuffer();
        for (String s : result) {
            if (s != null)
                multiline.append(s + "\n");
        }
        drawMultilineString(multiline.toString());
    }

    public void clear() throws IOException  {
        oledDriver.clearRam();
    }

    public void drawKeyValues(String[][] pStr) throws IOException {
        int maxLength = 0;
        String[] result = new String[font.getScreenHeight()];
        for (int i = 0; i < pStr.length; i++) {
            String key = pStr[i][0];
            String val = pStr[i][1];
            //recalculate maxLength for next column
            if (maxLength == 0 || (i % font.getScreenHeight() == 0)) {
                int tempKeyMax = 0;
                int tempValueMax = 0;
                for (int j = i; j < Math.min(i + font.getScreenHeight(), pStr.length); j++) {
                    if (tempKeyMax < pStr[j][0].length()) tempKeyMax = pStr[j][0].length();
                    if (tempValueMax < pStr[j][1].length()) tempValueMax = pStr[j][1].length();

                }
                maxLength = tempKeyMax + tempValueMax;
            }

            String res = key + generateWhitespaces(maxLength - key.length() - val.length()) + val;
            String oldVal = result[i % font.getScreenHeight()];
            if (oldVal != null) oldVal += COLUMN_DELIMETER + res;
            else oldVal = res;
            result[i % font.getScreenHeight()] = oldVal;
        }
        StringBuffer multiline = new StringBuffer();
        for (String s : result) {
            if (s != null)
                multiline.append(s + "\n");
        }
        drawMultilineString(multiline.toString());
    }

    private String generateWhitespaces(int length) {
        StringBuffer outputBuffer = new StringBuffer(length);
        outputBuffer.append(" ".repeat(Math.max(0, length)));
        return outputBuffer.toString();
    }

    public void drawMultilineString(String pStr) throws IOException {
        String[] strings = pStr.split("\n");
        int y = 0;
        for (int i = 0; i < font.getScreenHeight(); i++) {
            String s1 = (strings.length > i) ? strings[i] : "";
            //get string and adding trailing whitespaces to hide old values if it displayed there
            String s2 = String.format("%-" + font.getScreenWidth() + "s", s1);
            drawString(0, y, s2);
            y = y + font.getLineHeight();
        }
    }

    public void drawString(String pStr) throws IOException {
        int y = 0;
        for (int x = 0; x < font.getScreenHeight(); x++) {
            String temp = pStr.substring(0, Math.min(font.getScreenWidth(), pStr.length()));
            drawString(x, y, temp);
            pStr = pStr.substring(Math.min(font.getScreenWidth(), pStr.length()));
            if (pStr.length() == 0) return;
            y = y + font.getLineHeight();
        }
    }

    public void drawString(int x, int y, String pStr) throws IOException {
        for (char c : pStr.toCharArray()) {
            drawSingleAscii(x, y, c);
            x = x + 2;
        }
    }


    private void dataProcessing(int temp) throws IOException {
        int temp1 = temp & 0x80;
        int temp2 = (temp & 0x40) >> 3;
        int temp3 = (temp & 0x20) << 2;
        int temp4 = (temp & 0x10) >> 1;
        int temp5 = (temp & 0x08) << 4;
        int temp6 = (temp & 0x04) << 1;
        int temp7 = (temp & 0x02) << 6;
        int temp8 = (temp & 0x01) << 3;
        int h11 = temp1 | (temp1 >> 1) | (temp1 >> 2) | (temp1 >> 3);
        int h12 = temp2 | (temp2 >> 1) | (temp2 >> 2) | (temp2 >> 3);
        int h13 = temp3 | (temp3 >> 1) | (temp3 >> 2) | (temp3 >> 3);
        int h14 = temp4 | (temp4 >> 1) | (temp4 >> 2) | (temp4 >> 3);
        int h15 = temp5 | (temp5 >> 1) | (temp5 >> 2) | (temp5 >> 3);
        int h16 = temp6 | (temp6 >> 1) | (temp6 >> 2) | (temp6 >> 3);
        int h17 = temp7 | (temp7 >> 1) | (temp7 >> 2) | (temp7 >> 3);
        int h18 = temp8 | (temp8 >> 1) | (temp8 >> 2) | (temp8 >> 3);
        int d1 = h11 | h12;
        int d2 = h13 | h14;
        int d3 = h15 | h16;
        int d4 = h17 | h18;

        oledDriver.writeData(d1);
        oledDriver.writeData(d2);
        oledDriver.writeData(d3);
        oledDriver.writeData(d4);
    }


}
