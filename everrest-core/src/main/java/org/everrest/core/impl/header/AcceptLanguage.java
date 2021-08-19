/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.header;

import jakarta.ws.rs.ext.RuntimeDelegate;
import java.util.Locale;
import org.everrest.core.header.QualityValue;

/** @author andrew00x */
public class AcceptLanguage implements QualityValue {
  /** Default accepted language, it minds any language is acceptable. */
  public static final AcceptLanguage DEFAULT = new AcceptLanguage(new Locale("*"));

  /**
   * Creates a new instance of AcceptedLanguage by parsing the supplied string.
   *
   * @param header accepted language string
   * @return AcceptedLanguage
   */
  public static AcceptLanguage valueOf(String header) {
    return RuntimeDelegate.getInstance()
        .createHeaderDelegate(AcceptLanguage.class)
        .fromString(header);
  }

  private final Language language;
  /** Quality value for 'accepted' HTTP headers, e. g. en-gb;0.9 */
  private final float qValue;

  /**
   * Constructs new instance of accepted language with default quality value.
   *
   * @param language the language
   */
  public AcceptLanguage(Language language) {
    this.language = language;
    qValue = DEFAULT_QUALITY_VALUE;
  }

  /**
   * Constructs new instance of accepted language with quality value.
   *
   * @param language the language
   * @param qValue quality value
   */
  public AcceptLanguage(Language language, float qValue) {
    this.language = language;
    this.qValue = qValue;
  }

  public AcceptLanguage(Locale locale, float qValue) {
    this(new Language(locale), qValue);
  }

  public AcceptLanguage(Locale locale) {
    this(new Language(locale));
  }

  public Language getLanguage() {
    return language;
  }

  public String getPrimaryTag() {
    return language.getPrimaryTag();
  }

  public String getSubTag() {
    return language.getSubTag();
  }

  public Locale getLocale() {
    return language.getLocale();
  }

  public boolean isCompatible(AcceptLanguage other) {
    if (other == null) {
      return false;
    }
    return isCompatible(other.getLanguage());
  }

  public boolean isCompatible(Language other) {
    return language.isCompatible(other);
  }

  @Override
  public float getQvalue() {
    return qValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AcceptLanguage)) {
      return false;
    }

    AcceptLanguage other = (AcceptLanguage) o;
    return Float.compare(other.qValue, qValue) == 0 && language.equals(other.language);
  }

  @Override
  public int hashCode() {
    int hashcode = 8;
    hashcode = hashcode * 31 + (qValue == 0.0F ? 0 : Float.floatToIntBits(qValue));
    hashcode = hashcode * 31 + language.hashCode();
    return hashcode;
  }

  @Override
  public String toString() {
    return RuntimeDelegate.getInstance().createHeaderDelegate(AcceptLanguage.class).toString(this);
  }
}
