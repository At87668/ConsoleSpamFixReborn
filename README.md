
A bukkit/folia utility plugin that allows you to hide desired messages or errors that appear on server console and logs. The messages or errors are only hidden and not fixed! Useful if you want to hide errors or messages that spam the server console and get huge sized server logs. This plugin was made to stop console spam when you can't have the issue fixed, to prevent other issues because of intense console spam and unreadable console logs. It's very useful on modded servers where some mods can cause a lot of console spam. `This does not hide all errors! Only the ones you configure to be hidden!`

***Note!** On modded servers some errors my not be hidden by this plugin due the fact that mods run on forge and some console messages are sent outside the main server thread. If you have messages that this plugin can't hide please post them on the Discussion page.*



**How to update your ConsoleSpamFix to ConsoleSpamFixReborn?**

1. Download **ConsoleSpamFixReborn**.
1. Put **ConsoleSpamFixReborn** to `plugins/` folder
1. Rename `plugins/ConsoleSpamFix` to `plugins/ConsoleSpamFixReborn`
2. Enjoy!



**Do you want a powerful anti-xray?** Then check out my other plugin:

[![MinerTrack_64x.png](https://at87668.github.io/MinerTrack/Images/MinerTrack_64x.png) MinerTrack Anti-XRay - Minecraft Plugin (modrinth.com)](https://modrinth.com/plugin/minertrack)



**Features**

- The plugin should work fine on any bukkit/spigot server no mater the version. It works on Arclight modded servers too.
- Hides configured messages from appearing on server console and logs.
- Easy to configure. Check the plugin config file.
- Reload command to apply the config changes without a server restart.
- Performance friendly, no TPS drops.


**Install instructions**

- Place the plugin in the server plugins folder.
- Start the server and the config file will be generated on the first run.
- Change the config settings anytime and run the command /csf reload to apply the changes.
- Done, enjoy.



**Permissions:**

*csf.admin* - Allow the use of config reload command. Default to OP.

**Commands:**

*/csf reload* - *csf.admin* - Reload the plugin config.

**Configuration example:**

```yaml
# default config.yml for ConsoleSpamFixReborn
# config version 1.11.0

#Add here the messages you want to hide from your console and logs.
#Each line represents a new message.
#NOTE! You dont need to add the full message!
#The plugin will hide all messages that contain the text added below.
#  EXAMPLE:
#
#    Messages-To-Hide-Filter:
#    - 'No new version available'
#
#  All messages that will appear in the server console that contain these words 'WARNING: unlocalizedName' will be hidden and they will not appear on logs or console.
#  If you have large errors you dont have to type the full error. But the more words you add the more accurate will be and will not hide other messages that may contain the words added in the filter.

Messages-To-Hide-Filter:
- 'First example message'
- 'Second example message'


#Chat messages with color and format codes support.
#Minecraft Color and Format Codes: http://minecraft.gamepedia.com/Formatting_codes    
ChatMessages:
    NoPermission: '[System] You don''t have permission!'
    CmdHelp: '[System] Available commands: /csf reload'
    CmdReload: '[System] Config reload complete!'
```

**Note!**

This plugin **does not hide all error messages** from the server console and logs! 

This plugin hides **only the messages/errors you add to the config file!**

#### bStats

##### Bukkit
[![bukkit](https://bstats.org/signatures/bukkit/ConsoleSpamFixReborn.svg)](https://bstats.org/plugin/bukkit/ConsoleSpamFixReborn/24348)

##### Velocity
[![velocity](https://bstats.org/signatures/velocity/ConsoleSpamFixReborn.svg)](https://bstats.org/plugin/velocity/ConsoleSpamFixReborn/25291)