/*
 * Copyright 1999,2005 The Apache Software Foundation.
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

package org.apache.log4j.rolling;

import org.apache.log4j.Appender;
import org.apache.log4j.pattern.DatePatternConverter;
import org.apache.log4j.pattern.PatternConverter;
import org.apache.log4j.rolling.helper.*;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;

import java.util.Date;
import java.util.List;


/**
 * <code>TimeBasedRollingPolicy</code> is both easy to configure and quite
 * powerful.
 *
 * <p>In order to use  <code>TimeBasedRollingPolicy</code>, the
 * <b>FileNamePattern</b> option must be set. It basically specifies the name of the
 * rolled log files. The value <code>FileNamePattern</code> should consist of
 * the name of the file, plus a suitably placed <code>%d</code> conversion
 * specifier. The <code>%d</code> conversion specifier may contain a date and
 * time pattern as specified by the {@link java.text.SimpleDateFormat} class. If
 * the date and time pattern is ommitted, then the default pattern of
 * "yyyy-MM-dd" is assumed. The following examples should clarify the point.
 *
 * <p>
 * <table cellspacing="5px" border="1">
 *   <tr>
 *     <th><code>FileNamePattern</code> value</th>
 *     <th>Rollover schedule</th>
 *     <th>Example</th>
 *   </tr>
 *   <tr>
 *     <td nowrap="true"><code>/wombat/folder/foo.%d</code></td>
 *     <td>Daily rollover (at midnight).  Due to the omission of the optional
 *         time and date pattern for the %d token specifier, the default pattern
 *         of "yyyy-MM-dd" is assumed, which corresponds to daily rollover.
 *     </td>
 *     <td>During November 23rd, 2004, logging output will go to
 *       the file <code>/wombat/foo.2004-11-23</code>. At midnight and for
 *       the rest of the 24th, logging output will be directed to
 *       <code>/wombat/foo.2004-11-24</code>.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td nowrap="true"><code>/wombat/foo.%d{yyyy-MM}.log</code></td>
 *     <td>Rollover at the beginning of each month.</td>
 *     <td>During the month of October 2004, logging output will go to
 *     <code>/wombat/foo.2004-10.log</code>. After midnight of October 31st
 *     and for the rest of November, logging output will be directed to
 *       <code>/wombat/foo.2004-11.log</code>.
 *     </td>
 *   </tr>
 * </table>
 * <h2>Automatic file compression</h2>
 * <code>TimeBasedRollingPolicy</code> supports automatic file compression.
 * This feature is enabled if the value of the <b>FileNamePattern</b> option
 * ends with <code>.gz</code> or <code>.zip</code>.
 * <p>
 * <table cellspacing="5px" border="1">
 *   <tr>
 *     <th><code>FileNamePattern</code> value</th>
 *     <th>Rollover schedule</th>
 *     <th>Example</th>
 *   </tr>
 *   <tr>
 *     <td nowrap="true"><code>/wombat/foo.%d.gz</code></td>
 *     <td>Daily rollover (at midnight) with automatic GZIP compression of the
 *      archived files.</td>
 *     <td>During November 23rd, 2004, logging output will go to
 *       the file <code>/wombat/foo.2004-11-23</code>. However, at midnight that
 *       file will be compressed to become <code>/wombat/foo.2004-11-23.gz</code>.
 *       For the 24th of November, logging output will be directed to
 *       <code>/wombat/folder/foo.2004-11-24</code> until its rolled over at the
 *       beginning of the next day.
 *     </td>
 *   </tr>
 * </table>
 *
 * <h2>Decoupling the location of the active log file and the archived log files</h2>
 * <p>The <em>active file</em> is defined as the log file for the current period
 * whereas <em>archived files</em> are those files which have been rolled over
 * in previous periods.
 *
 * <p>By setting the <b>ActiveFileName</b> option you can decouple the location
 * of the active log file and the location of the archived log files.
 * <p>
 *  <table cellspacing="5px" border="1">
 *   <tr>
 *     <th><code>FileNamePattern</code> value</th>
 *     <th>ActiveFileName</th>
 *     <th>Rollover schedule</th>
 *     <th>Example</th>
 *   </tr>
 *   <tr>
 *     <td nowrap="true"><code>/wombat/foo.log.%d</code></td>
 *     <td nowrap="true"><code>/wombat/foo.log</code></td>
 *     <td>Daily rollover.</td>
 *
 *     <td>During November 23rd, 2004, logging output will go to
 *       the file <code>/wombat/foo.log</code>. However, at midnight that file
 *       will archived as <code>/wombat/foo.log.2004-11-23</code>. For the 24th
 *       of November, logging output will be directed to
 *       <code>/wombat/folder/foo.log</code> until its archived as
 *       <code>/wombat/foo.log.2004-11-24</code> at the beginning of the next
 *       day.
 *     </td>
 *   </tr>
 * </table>
 * <p>
 * If configuring programatically, do not forget to call {@link #activateOptions}
 * method before using this policy. Moreover, {@link #activateOptions} of
 * <code> TimeBasedRollingPolicy</code> must be called <em>before</em> calling
 * the {@link #activateOptions} method of the owning
 * <code>RollingFileAppender</code>.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Curt Arnold
 * @since 1.3
 */
public final class TimeBasedRollingPolicy extends RollingPolicyBase
  implements TriggeringPolicy {
  /**
   * Error message.
   */
  private static final String FNP_NOT_SET =
    "The FileNamePattern option must be set before using TimeBasedRollingPolicy. ";

  /**
   *   Reference for error message.
   */
  private static final String SEE_FNP_NOT_SET =
    "See also http://logging.apache.org/log4j/codes.html#tbr_fnp_not_set";

  /**
   * Time for next determination if time for rollover.
   */
  private long nextCheck = 0;

  /**
   * File name at last rollover.
   */
  private String lastFileName = null;

  /**
   * Length of any file type suffix (.gz, .zip).
   */
  private int suffixLength = 0;

  /**
   * Constructs a new instance.
   */
  public TimeBasedRollingPolicy() {
  }

  /**
   * Prepares instance of use.
   */
  public void activateOptions() {
    // find out period from the filename pattern
    if (fileNamePatternStr != null) {
      parseFileNamePattern();
    } else {
      getLogger().warn(FNP_NOT_SET);
      getLogger().warn(SEE_FNP_NOT_SET);
      throw new IllegalStateException(FNP_NOT_SET + SEE_FNP_NOT_SET);
    }

    PatternConverter dtc = null;

    for (int i = 0; i < patternConverters.length; i++) {
      if (patternConverters[i] instanceof DatePatternConverter) {
        dtc = patternConverters[i];

        break;
      }
    }

    if (dtc == null) {
      throw new IllegalStateException(
        "FileNamePattern [" + fileNamePatternStr
        + "] does not contain a valid date format specifier");
    }

    long n = System.currentTimeMillis();
    StringBuffer buf = new StringBuffer();
    formatFileName(new Date(n), buf);
    lastFileName = buf.toString();

    suffixLength = 0;

    if (lastFileName.endsWith(".gz")) {
      suffixLength = 3;
    } else if (lastFileName.endsWith(".zip")) {
      suffixLength = 3;
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean rollover(
    final StringBuffer activeFile, final List synchronousActions,
    final List asynchronousActions) {
    long n = System.currentTimeMillis();
    nextCheck = ((n / 1000) + 1) * 1000;

    StringBuffer buf = new StringBuffer();
    formatFileName(new Date(n), buf);

    String newFileName = buf.toString();

    //
    //  if file names haven't changed, no rollover
    //
    if (newFileName.equals(lastFileName)) {
      activeFile.setLength(0);

      if (activeFileName == null) {
        activeFile.append(
          newFileName.substring(0, newFileName.length() - suffixLength));
      } else {
        activeFile.append(activeFileName);
      }

      return false;
    }

    File lastBaseFile =
      new File(
        lastFileName.substring(0, lastFileName.length() - suffixLength));

    boolean lastFileExists = false;

    //
    //   if no explicit active file name then
    //      change active file name to new name
    //
    if (activeFileName == null) {
      lastFileExists = lastBaseFile.exists();
      activeFile.setLength(0);
      activeFile.append(newFileName);

      if (suffixLength > 0) {
        activeFile.setLength(activeFile.length() - suffixLength);
      }
    } else {
      activeFile.setLength(0);
      activeFile.append(activeFileName);

      File currentActiveFile = new File(activeFileName);
      lastFileExists = currentActiveFile.exists();
      synchronousActions.add(
        new FileRenameAction(currentActiveFile, lastBaseFile, true));
    }

    if ((suffixLength > 0) && lastFileExists) {
      File compressedFile = new File(lastFileName);

      if (suffixLength == 3) {
        asynchronousActions.add(
          new GZCompressAction(
            lastBaseFile, compressedFile, true, getLogger()));
      } else if (suffixLength == 4) {
        asynchronousActions.add(
          new ZipCompressAction(
            lastBaseFile, compressedFile, true, getLogger()));
      }
    }

    lastFileName = newFileName;

    return true;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isTriggeringEvent(
    final Appender appender, final LoggingEvent event, final String filename,
    final long fileLength) {
    return System.currentTimeMillis() >= nextCheck;
  }
}
