<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="text" indent="yes" encoding="iso-8859-1" />
  <!--<xsl:strip-space elements="*" />-->
  <xsl:template match="export"><xsl:apply-templates select="Diagnostic"/></xsl:template>
  <xsl:template match="Diagnostic"><xsl:apply-templates/><xsl:text>
</xsl:text></xsl:template>
  <xsl:template match="*|text()"><xsl:value-of select="."/>;<xsl:apply-templates/></xsl:template>
</xsl:stylesheet>
