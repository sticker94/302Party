
package com.party302;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Party302")
public interface Party302Config extends Config
{
	@ConfigItem(
			keyName = "token",
			name = "Token",
			description = "Enter the token provided by the web application",
			position = 1
	)
	default String token()
	{
		return "";
	}
}
