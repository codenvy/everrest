<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" encoding="UTF-8"/>
  <xsl:template match="/">
    <html>
      <head>
        <title>Book's</title>
      </head>
      <body>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="book">
    <table>
        <xsl:apply-templates select="//title"/>
        <xsl:apply-templates select="//author"/>
        <xsl:apply-templates select="//price"/>
        <xsl:apply-templates select="//member-price"/>
    </table>
    <xsl:choose>
      <xsl:when test="./@send-by-post='true'">
        <p>This book can be sent by post.</p>
      </xsl:when>
      <xsl:otherwise>
        <p>This book can't be sent by post.</p>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- Title -->
  <xsl:template match="title">
    <tr>
      <td>Title: </td>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  <!-- Author -->
  <xsl:template match="author">
    <tr>
      <td>Author: </td>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  <!-- Price -->
  <xsl:template match="price">
    <tr>
      <td>Price: </td>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  <!-- Memebers Price -->
  <xsl:template match="member-price">
    <tr>
      <td><i>Members Price: </i></td>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
