/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.internal.suffix;

import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.io.segment.BufferSource;
import org.aoju.bus.core.io.segment.GzipSource;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.http.Internal;
import org.aoju.bus.http.accord.platform.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.IDN;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A database of public suffixes provided by
 * <a href="https://publicsuffix.org/">suffix.org</a>.
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public final class SuffixDatabase {

    public static final String PUBLIC_SUFFIX_RESOURCE = "suffixes.gz";

    private static final byte[] WILDCARD_LABEL = new byte[]{'*'};
    private static final String[] EMPTY_RULE = new String[0];
    private static final String[] PREVAILING_RULE = new String[]{"*"};

    private static final byte EXCEPTION_MARKER = '!';

    private static final SuffixDatabase instance = new SuffixDatabase();

    /**
     * True after we've attempted to read the list for the first time.
     */
    private final AtomicBoolean listRead = new AtomicBoolean(false);

    /**
     * Used for concurrent threads reading the list for the first time.
     */
    private final CountDownLatch readCompleteLatch = new CountDownLatch(1);

    // The lists are held as a large array of UTF-8 bytes. This is to avoid allocating lots of strings
    // that will likely never be used. Each rule is separated by '\n'. Please see the
    // PublicSuffixListGenerator class for how these lists are generated.
    // Guarded by this.
    private byte[] publicSuffixListBytes;
    private byte[] publicSuffixExceptionListBytes;

    public static SuffixDatabase get() {
        return instance;
    }

    private static String binarySearchBytes(byte[] bytesToSearch, byte[][] labels, int labelIndex) {
        int low = 0;
        int high = bytesToSearch.length;
        String match = null;
        while (low < high) {
            int mid = (low + high) / 2;
            // Search for a '\n' that marks the start of a value. Don't go back past the start of the
            // array.
            while (mid > -1 && bytesToSearch[mid] != '\n') {
                mid--;
            }
            mid++;

            // Now look for the ending '\n'.
            int end = 1;
            while (bytesToSearch[mid + end] != '\n') {
                end++;
            }
            int publicSuffixLength = (mid + end) - mid;

            // Compare the bytes. Note that the file stores UTF-8 encoded bytes, so we must compare the
            // unsigned bytes.
            int compareResult;
            int currentLabelIndex = labelIndex;
            int currentLabelByteIndex = 0;
            int publicSuffixByteIndex = 0;

            boolean expectDot = false;
            while (true) {
                int byte0;
                if (expectDot) {
                    byte0 = '.';
                    expectDot = false;
                } else {
                    byte0 = labels[currentLabelIndex][currentLabelByteIndex] & 0xff;
                }

                int byte1 = bytesToSearch[mid + publicSuffixByteIndex] & 0xff;

                compareResult = byte0 - byte1;
                if (compareResult != 0) break;

                publicSuffixByteIndex++;
                currentLabelByteIndex++;
                if (publicSuffixByteIndex == publicSuffixLength) break;

                if (labels[currentLabelIndex].length == currentLabelByteIndex) {
                    // We've exhausted our current label. Either there are more labels to compare, in which
                    // case we expect a dot as the next character. Otherwise, we've checked all our labels.
                    if (currentLabelIndex == labels.length - 1) {
                        break;
                    } else {
                        currentLabelIndex++;
                        currentLabelByteIndex = -1;
                        expectDot = true;
                    }
                }
            }

            if (compareResult < 0) {
                high = mid - 1;
            } else if (compareResult > 0) {
                low = mid + end + 1;
            } else {
                // We found a match, but are the lengths equal?
                int publicSuffixBytesLeft = publicSuffixLength - publicSuffixByteIndex;
                int labelBytesLeft = labels[currentLabelIndex].length - currentLabelByteIndex;
                for (int i = currentLabelIndex + 1; i < labels.length; i++) {
                    labelBytesLeft += labels[i].length;
                }

                if (labelBytesLeft < publicSuffixBytesLeft) {
                    high = mid - 1;
                } else if (labelBytesLeft > publicSuffixBytesLeft) {
                    low = mid + end + 1;
                } else {
                    // Found a match.
                    match = new String(bytesToSearch, mid, publicSuffixLength, Charset.UTF_8);
                    break;
                }
            }
        }
        return match;
    }

    public String getEffectiveTldPlusOne(String domain) {
        if (domain == null) throw new NullPointerException("entity == null");

        // We use UTF-8 in the list so we need to convert to Unicode.
        String unicodeDomain = IDN.toUnicode(domain);
        String[] domainLabels = unicodeDomain.split("\\.");
        String[] rule = findMatchingRule(domainLabels);
        if (domainLabels.length == rule.length && rule[0].charAt(0) != EXCEPTION_MARKER) {
            // The entity is a public suffix.
            return null;
        }

        int firstLabelOffset;
        if (rule[0].charAt(0) == EXCEPTION_MARKER) {
            // Exception rules hold the effective TLD plus first.
            firstLabelOffset = domainLabels.length - rule.length;
        } else {
            // Otherwise the rule is for a public suffix, so we must take first more label.
            firstLabelOffset = domainLabels.length - (rule.length + 1);
        }

        StringBuilder effectiveTldPlusOne = new StringBuilder();
        String[] punycodeLabels = domain.split("\\.");
        for (int i = firstLabelOffset; i < punycodeLabels.length; i++) {
            effectiveTldPlusOne.append(punycodeLabels[i]).append('.');
        }
        effectiveTldPlusOne.deleteCharAt(effectiveTldPlusOne.length() - 1);

        return effectiveTldPlusOne.toString();
    }

    private String[] findMatchingRule(String[] domainLabels) {
        if (!listRead.get() && listRead.compareAndSet(false, true)) {
            readTheListUninterruptibly();
        } else {
            try {
                readCompleteLatch.await();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt(); // Retain interrupted status.
            }
        }

        synchronized (this) {
            if (publicSuffixListBytes == null) {
                throw new IllegalStateException("Unable to load " + PUBLIC_SUFFIX_RESOURCE + " resource "
                        + "from the classpath.");
            }
        }

        // Break apart the entity into UTF-8 labels, i.e. foo.bar.com turns into [foo, bar, com].
        byte[][] domainLabelsUtf8Bytes = new byte[domainLabels.length][];
        for (int i = 0; i < domainLabels.length; i++) {
            domainLabelsUtf8Bytes[i] = domainLabels[i].getBytes(Charset.UTF_8);
        }

        // Start by looking for exact matches. We start at the leftmost label. For example, foo.bar.com
        // will look like: [foo, bar, com], [bar, com], [com]. The longest matching rule wins.
        String exactMatch = null;
        for (int i = 0; i < domainLabelsUtf8Bytes.length; i++) {
            String rule = binarySearchBytes(publicSuffixListBytes, domainLabelsUtf8Bytes, i);
            if (rule != null) {
                exactMatch = rule;
                break;
            }
        }

        // In theory, wildcard rules are not restricted to having the wildcard in the leftmost position.
        // In practice, wildcards are always in the leftmost position. For now, this implementation
        // cheats and does not attempt every possible permutation. Instead, it only considers wildcards
        // in the leftmost position. We assert this fact when we generate the public suffix file. If
        // this assertion ever fails we'll need to refactor this implementation.
        String wildcardMatch = null;
        if (domainLabelsUtf8Bytes.length > 1) {
            byte[][] labelsWithWildcard = domainLabelsUtf8Bytes.clone();
            for (int labelIndex = 0; labelIndex < labelsWithWildcard.length - 1; labelIndex++) {
                labelsWithWildcard[labelIndex] = WILDCARD_LABEL;
                String rule = binarySearchBytes(publicSuffixListBytes, labelsWithWildcard, labelIndex);
                if (rule != null) {
                    wildcardMatch = rule;
                    break;
                }
            }
        }

        // Exception rules only apply to wildcard rules, so only try it if we matched a wildcard.
        String exception = null;
        if (wildcardMatch != null) {
            for (int labelIndex = 0; labelIndex < domainLabelsUtf8Bytes.length - 1; labelIndex++) {
                String rule = binarySearchBytes(
                        publicSuffixExceptionListBytes, domainLabelsUtf8Bytes, labelIndex);
                if (rule != null) {
                    exception = rule;
                    break;
                }
            }
        }

        if (exception != null) {
            // Signal we've identified an exception rule.
            exception = "!" + exception;
            return exception.split("\\.");
        } else if (exactMatch == null && wildcardMatch == null) {
            return PREVAILING_RULE;
        }

        String[] exactRuleLabels = exactMatch != null
                ? exactMatch.split("\\.")
                : EMPTY_RULE;

        String[] wildcardRuleLabels = wildcardMatch != null
                ? wildcardMatch.split("\\.")
                : EMPTY_RULE;

        return exactRuleLabels.length > wildcardRuleLabels.length
                ? exactRuleLabels
                : wildcardRuleLabels;
    }

    private void readTheListUninterruptibly() {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    readTheList();
                    return;
                } catch (InterruptedIOException e) {
                    Thread.interrupted(); // Temporarily clear the interrupted state.
                    interrupted = true;
                } catch (IOException e) {
                    Platform.get().log(Platform.WARN, "Failed to read public suffix list", e);
                    return;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt(); // Retain interrupted status.
            }
        }
    }

    private void readTheList() throws IOException {
        byte[] publicSuffixListBytes;
        byte[] publicSuffixExceptionListBytes;

        InputStream resource = SuffixDatabase.class.getResourceAsStream(PUBLIC_SUFFIX_RESOURCE);
        if (resource == null) return;

        BufferSource bufferedSource = IoUtils.buffer(new GzipSource(IoUtils.source(resource)));
        try {
            int totalBytes = bufferedSource.readInt();
            publicSuffixListBytes = new byte[totalBytes];
            bufferedSource.readFully(publicSuffixListBytes);

            int totalExceptionBytes = bufferedSource.readInt();
            publicSuffixExceptionListBytes = new byte[totalExceptionBytes];
            bufferedSource.readFully(publicSuffixExceptionListBytes);
        } finally {
            Internal.closeQuietly(bufferedSource);
        }

        synchronized (this) {
            this.publicSuffixListBytes = publicSuffixListBytes;
            this.publicSuffixExceptionListBytes = publicSuffixExceptionListBytes;
        }

        readCompleteLatch.countDown();
    }

    void setListBytes(byte[] publicSuffixListBytes, byte[] publicSuffixExceptionListBytes) {
        this.publicSuffixListBytes = publicSuffixListBytes;
        this.publicSuffixExceptionListBytes = publicSuffixExceptionListBytes;
        listRead.set(true);
        readCompleteLatch.countDown();
    }

}
