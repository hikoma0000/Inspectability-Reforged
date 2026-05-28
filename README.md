# Inspectability Reforged

<img src="https://github.com/hikoma0000/Inspectability-Reforged/blob/gallery/demo.gif?raw=true" width="100%">

This mod is an unofficial port of [gifluana/Inspectability](https://github.com/gifluana/Inspectability), featuring numerous new features and improvements.

<a href="https://minecraftforge.net/"><img alt="forge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/forge_vector.svg"></a> <a href="https://fabricmc.net/"><img alt="fabric" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg"></a> <a href="https://modrinth.com/mod/architectury-api"><img alt="architectury-api" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/architectury-api_vector.svg"></a>

<a href="https://github.com/hikoma0000/Inspectability-Reforged/issues"><img alt="issues-and-suggestions-on-github" src="https://raw.githubusercontent.com/hikoma0000/MinecraftBadges/e83b7ab154dab909ec85e99091282b02bff3bff3/github/issues_and_suggestions_on_github.svg"></a>

Ever wanted to **zoom in, rotate, and inspect** your favorite item models up close — right inside the game?

**Inspectability Reforged** is a lightweight client-side mod that lets players open a 3D inspector screen for any item, allowing full control over rotation, position, and zoom. Whether you're a texture artist, model maker, content creator, or just a curious explorer — this mod gives you the perfect tool to appreciate your items in detail.

---

## 🛠 Features

### Core Features
- 🎥 Smooth and intuitive 3D viewer with **mouse control**
- 🔍 Adjust **scale**, **rotation**, and **offset**
- 🧠 Ideal for creators testing resource packs or mods
- 🧼 No server-side installation needed — **client-only**

### ✨ Reforged Additions
- 🐄 **Entity & Equipment Inspection Support**: Spawn Eggs, Minecarts, Boats, Armor, and more are now dynamically rendered as their actual entities instead of flat items.
- 🎨 **Automatic Variant Support**: Dynamic support for entities with multiple variants (like Paintings, Frogs, Cats, etc.) without manual configuration.
- ⚙️ **Inspector Builder API**: A powerful API allowing other modders to register custom viewing logic, entities, and variants with just a few lines of code.
- 🤸 **Roll Rotation**: Hold `Ctrl` + Left-Click horizontal drag to roll the item.

### 🚀 Planned Features
- ⬜ **JEI, REI, and EMI** compatibility
- ⬜ **Timeless and Classics Zero (TaC:Z)** compatibility
- ⬜ **Alex's Mobs** compatibility
- ⬜ **Create** compatibility
- ⬜ **Interactive Mode** implementation
- ⬜ **Entity Inspector Magnifying Glass & Record Book** (Planned to be released as a separate server-side add-on for Survival mode, requiring this mod as a dependency)

<!--✅⬜--->

---

## ❓ Frequently Asked Questions

**Q: Is this mod compatible with modpacks and other content?**  
**A:** Yes! You can use Inspectability in any modpack or video content. Just credit it properly.

**Q: Can I use it to inspect modded items too?**  
**A:** Absolutely. If the item has a model, Inspectability can show it. With the new Inspector Builder API, modders can also easily add custom logic for their items!

**Q: Does this affect gameplay?**  
**A:** Not at all. It's entirely optional and cosmetic — just open the viewer when you want.

---

<details>
<summary><h2>👨‍💻 For Developers</h2></summary>

Inspectability Reforged provides an easy-to-use API to help you integrate inspection features into your own mods.

### InspectabilityAPI
You can easily hook into and open the inspection screen for specific `ItemStack`s, `Entity` objects, or even custom view states from your client-side code:
```java
import io.github.hikoma0000.inspectability.client.api.InspectabilityAPI;
import io.github.hikoma0000.inspectability.client.api.StandardViewState;
import java.util.List;

// 1. Open default inspector for an ItemStack
InspectabilityAPI.inspect(new ItemStack(Items.DIAMOND));

// 2. Open inspector for an Entity
Entity myEntity = EntityType.COW.create(Minecraft.getInstance().level);
InspectabilityAPI.inspect(myEntity);

// 3. Completely override the view states!
// You can freely pass ANY combination of states. 
// (e.g. only Block, or multiple custom Entities, or both)
InspectabilityAPI.inspect(new ItemStack(Items.CHEST), List.of(
    StandardViewState.BLOCK, // Show just the block form
    customEntityState1,      // Show a custom entity
    customEntityState2       // Show another custom entity
));
```

### InspectorStateRegistry
If you want to configure how your custom items or entities should be viewed by default when the user presses the inspect key, you can register custom View States during initialization:
```java
import io.github.hikoma0000.inspectability.client.api.InspectorStateRegistry;

InspectorStateRegistry.register(MyCustomItem.class, CustomViewStateConfig.builder("my_custom_view")
    .entity((mc, stack) -> new MyCustomEntity(mc.level))
    .build());
```

<!---📖 **For full documentation, see the [Developer Wiki](https://github.com/hikoma0000/Inspectability-Reforged/wiki).**--->
The full documentation is coming soon..

</details>

---

## License

The resources in this repository (including directories below) have been entirely recreated by hikoma0000. Therefore, the original "All Rights Reserved" restrictions have been removed, and these resources are now released under the MIT License.

/common/src/main/resources/
/fabric/src/main/resources/
/forge/src/main/resources/

---

## My Mods

[![](https://github.com/hikoma0000/Realistic-Nametag/blob/gallery/data/logo.png?raw=true)](https://www.curseforge.com/minecraft/mc-mods/realistic-nametag)[![](https://github.com/hikoma0000/Advancement-Trophies-Reforged/blob/gallery/data/logo.png?raw=true)](https://www.curseforge.com/minecraft/mc-mods/advancement-trophies-reforged)
