/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/atom/node-oniguruma
 * Initial copyright Copyright (c) 2013 GitHub Inc.
 * Initial license: MIT
 *
 * Contributors:
 * - GitHub Inc.: Initial code, written in JavaScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 * - Fabio Zadrozny <fabiofz@gmail.com> - Convert uniqueId to Object (for identity compare)
 * - Fabio Zadrozny <fabiofz@gmail.com> - Fix recursion error on creation of OnigRegExp with unicode chars
 */

package org.eclipse.tm4e.core.internal.oniguruma;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.TMException;
import org.jcodings.specific.UTF8Encoding;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;
import org.joni.Syntax;
import org.joni.WarnCallback;
import org.joni.exception.SyntaxException;

/**
 *
 * @see <a href="https://github.com/atom/node-oniguruma/blob/master/src/onig-reg-exp.cc">
 *      github.com/atom/node-oniguruma/blob/master/src/onig-reg-exp.cc</a> *
 */
final class OnigRegExp {

	@Nullable
	private OnigString lastSearchString;

	private int lastSearchPosition = -1;

	@Nullable
	private OnigResult lastSearchResult;

	private final Regex regex;

	OnigRegExp(final String source) {
		final byte[] pattern = source.getBytes(StandardCharsets.UTF_8);
		try {
			regex = new Regex(pattern, 0, pattern.length, Option.CAPTURE_GROUP, UTF8Encoding.INSTANCE, Syntax.DEFAULT,
					WarnCallback.DEFAULT);
		} catch (final SyntaxException ex) {
			throw new TMException("Parsing regex pattern \"" + source + "\" failed with " + ex, ex);
		}
	}

	@Nullable
	OnigResult search(final OnigString str, final int position) {
		final OnigResult theLastSearchResult = lastSearchResult;
		if (lastSearchString == str
				&& lastSearchPosition <= position
				&& (theLastSearchResult == null || theLastSearchResult.locationAt(0) >= position)) {
			return theLastSearchResult;
		}

		lastSearchString = str;
		lastSearchPosition = position;
		lastSearchResult = search(str.bytesUTF8, position, str.bytesCount);
		return lastSearchResult;
	}

	@Nullable
	private OnigResult search(final byte[] data, final int position, final int end) {
		final Matcher matcher = regex.matcher(data);
		final int status = matcher.search(position, end, Option.DEFAULT);
		if (status != Matcher.FAILED) {
			final Region region = matcher.getEagerRegion();
			return new OnigResult(region, -1);
		}
		return null;
	}
}
