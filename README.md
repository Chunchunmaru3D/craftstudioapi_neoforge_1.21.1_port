# CraftStudioAPI NeoForge 1.21.1 Port

This is an independent NeoForge 1.21.1 port of the original Forge 1.12.2 CraftStudioAPI. It is a runtime library for mods that use CraftStudio `.csjsmodel` geometry and `.csjsmodelanim` animation assets.

It is currently used by the Animania NeoForge port. The library is not a standalone content mod.

## Build

Requirements:

- Java 21

On Windows:

```powershell
.\gradlew.bat build
```

The output JAR is written to `build/libs/craftstudioapi-0.1.0.jar`. Copy that file to the consuming mod's `libs` directory when building without a Maven repository.

## Source layout

| Path | Purpose |
| --- | --- |
| `client/json` | CraftStudio JSON parsing and model/animation data structures. |
| `client/registry` | Resource-reload registry and on-demand model loading. |
| `client/model` | Conversion to Minecraft `LayerDefinition` and public baking helpers. |
| `client/animation` | Keyframe interpolation and application to `ModelPart` trees. |
| `client/exception` | Library-specific parsing and resource exceptions. |

## Supported runtime features

- Loads model assets from `assets/<namespace>/craftstudio/models/**/*.csjsmodel`.
- Loads animation assets from `assets/<namespace>/craftstudio/animations/**/*.csjsmodelanim`.
- Discovers assets from every loaded mod namespace during resource reload.
- Preserves parent pivots, offsets, explicit vertex coordinates, UV data, and node hierarchy when baking a `LayerDefinition`.
- Converts CraftStudio's node rotation order for Minecraft `ModelPart` rendering.
- Interpolates position, rotation, and offset keyframes.
- Performs an on-demand load when a layer is requested before the normal reload listener has populated its cache.

## Limitations and follow-up work

- The original 1.12.2 network animation layer is not ported. Consumers must derive animation time from their own synchronized state and call the public animation helper on the client.
- `SIZE` and `STRETCH` keyframes are not implemented. The Animania assets checked during this port use position, rotation, and offset tracks only.
- The library needs broader validation with independent CraftStudio mods and complex nested models. In particular, verify geometry, UVs, rotation order, and keyframe output in-game rather than relying on successful compilation.
- Consumer-side models still need their own porting work. A working library does not guarantee visual parity for the cart, wagon, tiller, hives, hamster wheel, or pet furniture in Animania.

## Notes for contributors

1. Keep this library independent from Animania; it is built and released separately.
2. Preserve the public resource-path convention and original JSON semantics unless a compatibility fix requires a change.
3. Test changes with a model that has nested nodes, non-zero pivot offsets, and simultaneous X/Y rotations.
4. Do not commit Gradle caches, build output, IDE metadata, or local run data.
