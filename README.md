# NashornCompatLayer
A simple toy to allow MinecraftForge running under Java 15.

## Usage
### For Client
1. Create a folder in `.minecraft/versions` directory with any name you want.
1. put [client.json](client-version-json/client.json) to the folder you just created and renamed it as the same as the folder.
1. Open the json file, modify `id` attribute to the same as the folder name.
1. Install [MinecraftForge](https://files.minecraftforge.net/) any versions you want for Minecraft 1.15.2+.
1. Modify `inheritsFrom` attribute to the same as the forge version.
1. Run your minecraft launcher, you will see a new version just created.
1. You can launch the game with Java 15+.

### For Server
1. Install [MinecraftForge](https://files.minecraftforge.net/) any versions you want for Minecraft 1.15.2+.
1. Download this thing and nashorn into `libraries/io/github/zekerzhayard/nashorn-compat-layer/dependencies` folder.
1. Start server with the command below: (require Java 15+)
```
java --add-modules org.openjdk.nashorn,org.objectweb.asm,org.objectweb.asm.commons,org.objectweb.asm.tree,org.objectweb.asm.tree.analysis,org.objectweb.asm.util \
    --enable-preview \
    --module-path libraries/io/github/zekerzhayard/nashorn-compat-layer/dependencies/ \
    -cp "<this thing, nashorn-core and forge launcher jar>"
    net.minecraftforge.server.ServerMain
```