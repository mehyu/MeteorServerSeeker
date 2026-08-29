package de.damcraft.serverseeker.country;

import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import de.damcraft.serverseeker.ServerSeeker;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Country implements Comparable<Country> {
    public final Identifier identifier;
    public final String name;
    public final String code;
    private final TextureData textureData;

    public Country(String name, String code) {
        this.name = name;
        this.code = code.toLowerCase(Locale.ENGLISH);
        this.identifier = Identifier.fromNamespaceAndPath("serverseeker", String.format("textures/flags/%s.png", this.code));
        if (mc.getResourceManager().getResource(this.identifier).isEmpty()) {
            ServerSeeker.LOG.error("Could not find flag for country: {}", this.code);
            this.textureData = new EmptyTextureData();
        } else this.textureData = new CountryTextureData();
    }

    @Nullable
    public Texture getTexture() {
        Texture texture = this.textureData.get();
        if (texture == null) return this == Countries.UN ? null : Countries.UN.getTexture();
        return texture;
    }

    public void dispose() {
        this.textureData.dispose();
    }

    @Override
    public int compareTo(@NotNull Country o) {
        return this.name.compareTo(o.name);
    }

    public sealed interface TextureData {
        @Nullable
        default Texture get() {
            return null;
        }

        default void dispose() {}
    }

    public final class CountryTextureData implements TextureData {
        private Texture texture = null;
        private State state = State.EMPTY;

        @Nullable
        @Override
        public Texture get() {
            if (this.state == State.DONE) return this.texture;
            else {
                if (this.state == State.EMPTY) MeteorExecutor.execute(this::load);
                return null;
            }
        }

        @Override
        public void dispose() {
            if (this.state == State.DONE) {
                this.texture.close();
                this.texture = null;
            }
            this.state = State.EMPTY;
        }

        private void load() {
            this.state = State.LOADING;
            Optional<Resource> resource = mc.getResourceManager().getResource(Country.this.identifier);
            if (resource.isEmpty()) {
                this.state = State.EMPTY;
                return;
            }

            try (InputStream imageStream = resource.get().open()) {
                BufferedImage bufferedImage = ImageIO.read(imageStream);

                int[] pixels = bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null, 0, bufferedImage.getWidth());
                byte[] data = new byte[bufferedImage.getWidth() * bufferedImage.getHeight() * 4];

                for (int i = 0; i < pixels.length; i++) {
                    int argb = pixels[i];
                    data[4 * i] = (byte) ((argb >> 16) & 0xFF); // r
                    data[4 * i + 1] = (byte) ((argb >>  8) & 0xFF); // g
                    data[4 * i + 2] = (byte) ((argb) & 0xFF); // b
                    data[4 * i + 3] = (byte) ((argb >> 24) & 0xFF); // a
                }

                // Texture creation and upload must happen on the render thread
                mc.execute(() -> {
                    Texture texture = new Texture(bufferedImage.getWidth(), bufferedImage.getHeight(), TextureFormat.RGBA8, FilterMode.NEAREST, FilterMode.NEAREST);
                    texture.upload(data);
                    this.texture = texture;
                    this.state = State.DONE;
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public enum State {
            EMPTY,
            LOADING,
            DONE
        }
    }

    public static final class EmptyTextureData implements TextureData {}
}
