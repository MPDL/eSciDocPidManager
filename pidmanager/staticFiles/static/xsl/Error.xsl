<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method='html' version='1.0' encoding='UTF-8'
		indent='yes' />

	<xsl:template match="/">
		<html>
			<head>
				<title>
					Exception:
					<xsl:value-of select="exception/title" />
				</title>
			</head>
			<body bgcolor="#DDDDDD">
				<div align="right">PIDManager</div>
				<hr noshadow="true" />
				<table border="0" width="60%">
					<tr>
						<td>Exception</td>
						<td>
							<font color="red" size="+2">
								<xsl:value-of select="exception/title" />
							</font>
						</td>
					</tr>
					<tr>
						<td>Message</td>
						<td>
							<xsl:value-of select="exception/message" />
						</td>
					</tr>
				</table>
			</body>
		</html>

	</xsl:template>

</xsl:stylesheet>