<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:sv="http://www.jcp.org/jcr/sv/1.0"
xmlns:xlink="http://www.w3.org/1999/xlink">
  <xsl:output method="html" encoding="UTF-8"/>
  <xsl:template match="/sv:node">
    <html>
      <head><title>WEBDAV Browser</title></head>
      <body>
        <div id="main">
          <p>Node : <xsl:value-of select="./@sv:name"/></p>
          <ul>
          <xsl:apply-templates select="sv:property">
            <xsl:sort order="ascending" select="./@sv:name"/>
          </xsl:apply-templates>
          </ul>
          <xsl:apply-templates select="sv:node">
            <xsl:sort order="ascending" select="./@sv:name"/>
          </xsl:apply-templates>
        </div>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="sv:node">
    <a><xsl:attribute name="href">
        <xsl:value-of select="./@xlink:href"/>
      </xsl:attribute>
      <xsl:value-of select="./@sv:name"/>
     </a><br/>
  </xsl:template>
  
  <xsl:template match="sv:property">
    <li><xsl:value-of select="./@sv:name"/></li><br/>
  </xsl:template>
  
</xsl:stylesheet>
