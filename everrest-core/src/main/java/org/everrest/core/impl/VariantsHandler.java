/*
 * Copyright (c) 2012-2022 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl;

import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT_CHARSET;
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT_LANGUAGE;
import static java.util.stream.Collectors.toList;
import static org.everrest.core.impl.header.HeaderHelper.convertToString;
import static org.everrest.core.impl.header.HeaderHelper.createAcceptMediaTypeList;
import static org.everrest.core.impl.header.HeaderHelper.createAcceptedCharsetList;
import static org.everrest.core.impl.header.HeaderHelper.createAcceptedEncodingList;
import static org.everrest.core.impl.header.HeaderHelper.createAcceptedLanguageList;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Variant;
import java.util.ArrayList;
import java.util.List;
import org.everrest.core.impl.header.AcceptLanguage;
import org.everrest.core.impl.header.AcceptMediaType;
import org.everrest.core.impl.header.AcceptToken;
import org.everrest.core.impl.header.Language;

public class VariantsHandler {
  /**
   * Looking for most acceptable variant for given request.
   *
   * @param request see {@link ContainerRequest}
   * @param variants see {@link Variant} and {@link VariantListBuilderImpl}
   * @return variant or null
   */
  public Variant handleVariants(ContainerRequest request, List<Variant> variants) {
    List<AcceptMediaType> acceptMediaTypes =
        createAcceptMediaTypeList(convertToString(request.getRequestHeader(ACCEPT)));
    List<AcceptLanguage> acceptLanguages =
        createAcceptedLanguageList(convertToString(request.getRequestHeader(ACCEPT_LANGUAGE)));
    List<AcceptToken> acceptCharset =
        createAcceptedCharsetList(convertToString(request.getRequestHeader(ACCEPT_CHARSET)));
    List<AcceptToken> acceptEncoding =
        createAcceptedEncodingList(convertToString(request.getRequestHeader(ACCEPT_ENCODING)));

    List<Variant> filteredVariants = new ArrayList<>(variants);

    filteredVariants = filterByMediaType(acceptMediaTypes, filteredVariants);
    filteredVariants = filterByLanguage(acceptLanguages, filteredVariants);
    filteredVariants = filterByCharset(acceptCharset, filteredVariants);
    filteredVariants = filterByEncoding(acceptEncoding, filteredVariants);

    return filteredVariants.isEmpty() ? null : filteredVariants.get(0);
  }

  private List<Variant> filterByMediaType(
      List<AcceptMediaType> acceptMediaTypes, List<Variant> variants) {
    List<Variant> filteredVariants = new ArrayList<>();
    acceptMediaTypes.forEach(
        acceptMediaType ->
            variants.forEach(
                variant -> {
                  if (variant.getMediaType() != null
                      && acceptMediaType.isCompatible(variant.getMediaType())) {
                    filteredVariants.add(variant);
                  }
                }));

    filteredVariants.addAll(
        variants.stream().filter(variant -> variant.getMediaType() == null).collect(toList()));

    return filteredVariants;
  }

  private List<Variant> filterByLanguage(
      List<AcceptLanguage> acceptLanguages, List<Variant> variants) {
    List<Variant> filteredVariants = new ArrayList<>();
    acceptLanguages.forEach(
        acceptLanguage ->
            variants.forEach(
                variant -> {
                  if (variant.getLanguage() != null
                      && acceptLanguage.isCompatible(new Language(variant.getLanguage()))) {
                    filteredVariants.add(variant);
                  }
                }));

    filteredVariants.addAll(
        variants.stream().filter(variant -> variant.getLanguage() == null).collect(toList()));

    return filteredVariants;
  }

  private List<Variant> filterByCharset(List<AcceptToken> acceptCharsets, List<Variant> variants) {
    List<Variant> filteredVariants = new ArrayList<>();
    acceptCharsets.forEach(
        acceptCharset ->
            variants.forEach(
                variant -> {
                  if (acceptCharset.isCompatible(getCharset(variant.getMediaType()))) {
                    filteredVariants.add(variant);
                  }
                }));

    filteredVariants.addAll(
        variants.stream()
            .filter(variant -> getCharset(variant.getMediaType()) == null)
            .collect(toList()));

    return filteredVariants;
  }

  private String getCharset(MediaType mediaType) {
    if (mediaType == null) {
      return null;
    }
    return mediaType.getParameters().get("charset");
  }

  private List<Variant> filterByEncoding(
      List<AcceptToken> acceptEncodings, List<Variant> variants) {
    List<Variant> filteredVariants = new ArrayList<>();
    acceptEncodings.forEach(
        acceptEncoding ->
            variants.forEach(
                variant -> {
                  if (acceptEncoding.isCompatible(variant.getEncoding())) {
                    filteredVariants.add(variant);
                  }
                }));

    filteredVariants.addAll(
        variants.stream().filter(variant -> variant.getEncoding() == null).collect(toList()));

    return filteredVariants;
  }
}
