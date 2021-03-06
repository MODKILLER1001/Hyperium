package cc.hyperium.handlers.handlers.animation.fortnite;

import cc.hyperium.event.InvokeEvent;
import cc.hyperium.event.WorldChangeEvent;
import cc.hyperium.handlers.handlers.animation.AbstractPreCopyAnglesAnimationHandler;
import cc.hyperium.handlers.handlers.animation.AnimationFrame;
import cc.hyperium.handlers.handlers.animation.BodyPart;
import cc.hyperium.handlers.handlers.animation.TwerkDance;
import cc.hyperium.handlers.handlers.chat.GeneralChatHandler;
import cc.hyperium.mixinsimp.renderer.model.IMixinModelBiped;
import cc.hyperium.mixinsimp.renderer.model.IMixinModelPlayer;
import cc.hyperium.utils.JsonHolder;
import cc.hyperium.utils.Multithreading;
import com.google.gson.JsonElement;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelRenderer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class FortniteDefaultDance extends AbstractPreCopyAnglesAnimationHandler {

    private List<AnimationFrame> frames = new ArrayList<>();
    private long duration;
    private HashMap<UUID, Long> states = new HashMap<>();
    private boolean loaded = false;

    public FortniteDefaultDance() {
        //starting frame
        Multithreading.runAsync(this::generateFrames);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TwerkDance.Frame(scanner);

    }

    //THE EPITOME OF PAIN. FOR YOUR OWN GOOD, DO NOT ATTEMPT
    public void generateFrames() {
        frames.clear();
        JsonHolder holder = null;
        try {
            holder = new JsonHolder(IOUtils.toString(new URL("https://static.sk1er.club/hyperium/fortnite_dance.json")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (holder == null) {
            GeneralChatHandler.instance().sendMessage("An error occurred while loading fortnite dance json");
            return;
        }
        for (JsonElement element : holder.optJSONArray("frames")) {
            JsonHolder h = new JsonHolder(element.getAsJsonObject());
            AnimationFrame frame = new AnimationFrame(frame(h.optInt("time")));
            frame.name = h.optInt("time") + "";
            for (String s : h.getKeys()) {
                if (!s.equalsIgnoreCase("time")) {
                    try {
                        Field declaredField1 = frame.getClass().getDeclaredField(s);
                        declaredField1.setAccessible(true);
                        BodyPart bodyPart = (BodyPart) declaredField1.get(frame);
                        JsonHolder holder1 = h.optJSONObject(s);
                        for (String s1 : holder1.getKeys()) {
                            Field declaredField = bodyPart.getClass().getDeclaredField(s1);
                            declaredField.setAccessible(true);
                            float f = (float) holder1.optDouble(s1);
                            declaredField.setFloat(bodyPart, f);
                        }
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }
            }
//            System.out.println("adding frame " + frame);
            this.frames.add(frame);
        }

        loaded = true;
//
        duration = frames.get(frames.size() - 1).getTime();
    }

    private float radians(int deg) {
        return (float) Math.toRadians(deg);
    }

    private long frame(int frame) {
        return frame * 1000 / 30;
    }

    @InvokeEvent
    public void worldSwap(WorldChangeEvent event) {
        states.clear();
    }

    public HashMap<UUID, Long> getStates() {
        return states;
    }

    @Override
    public float modifyState() {
        return 0;
    }

    @Override
    public void modifyPlayer(AbstractClientPlayer entity, IMixinModelPlayer player, float heldPercent) {
        if (!loaded)
            return;
        Long aLong = states.get(entity.getUniqueID());
        if (aLong == null || aLong == 0) {
            resetAnimation(player);
            get(entity.getUniqueID()).frames = 0;
            return;
        }
        long current = System.currentTimeMillis();
        long timeSinceStart = current - aLong;
        if (timeSinceStart > duration)
            states.remove(entity.getUniqueID());

        AnimationFrame prev = null;
        AnimationFrame next = null;
        for (AnimationFrame frame : frames) {
            if (prev == null || (frame.getTime() < timeSinceStart && frame.getTime() > prev.getTime())) {
                prev = frame;
            }
            if ((next == null && frame.getTime() > prev.getTime()) || (frame.getTime() > timeSinceStart && frame.getTime() < next.getTime())) {
                next = frame;
            }
        }
        if (prev == null || next == null) {
            return;
        }

        float v = (timeSinceStart - prev.getTime());
        long l = next.getTime() - prev.getTime();
        float percent = v / (float) l;


//        next = frames.get(frames.size() - 1);
//        percent = 1.0F;
//        System.out.println(prev.name + " -> " + next.name + " " + percent);
//        Right upper arm
        adjust(player.getBipedRightUpperArmwear(), prev.getRightUpperArm().calc(percent, next.getRightUpperArm()));
        adjust(player.getBipedRightUpperArm(), prev.getRightUpperArm().calc(percent, next.getRightUpperArm()));

        //Right lower arm
        adjust(player.getBipedRightForeArm(), prev.getRightLowerArm().calc(percent, next.getRightLowerArm()));
        adjust(player.getBipedRightForeArmwear(), prev.getRightLowerArm().calc(percent, next.getRightLowerArm()));

        //Left upper arm
        adjust(player.getBipedLeftUpperArmwear(), prev.getLeftUpperArm().calc(percent, next.getLeftUpperArm()));
        adjust(player.getBipedLeftUpperArm(), prev.getLeftUpperArm().calc(percent, next.getLeftUpperArm()));

        //Left lower arm
        adjust(player.getBipedLeftForeArm(), prev.getLeftLowerArm().calc(percent, next.getLeftLowerArm()));
        adjust(player.getBipedLeftForeArmwear(), prev.getLeftLowerArm().calc(percent, next.getLeftLowerArm()));


        //Right upper Leg
        adjust(player.getBipedRightUpperLegwear(), prev.getRightUpperLeg().calc(percent, next.getRightUpperLeg()));
        adjust(player.getBipedRightUpperLeg(), prev.getRightUpperLeg().calc(percent, next.getRightUpperLeg()));

        //Right lower Leg
        adjust(player.getBipedRightLowerLeg(), prev.getRightLowerLeg().calc(percent, next.getRightLowerLeg()));
        adjust(player.getBipedRightLowerLegwear(), prev.getRightLowerLeg().calc(percent, next.getRightLowerLeg()));

        //Left upper Leg
        adjust(player.getBipedLeftUpperLegwear(), prev.getLeftUpperLeg().calc(percent, next.getLeftUpperLeg()));
        adjust(player.getBipedLeftUpperLeg(), prev.getLeftUpperLeg().calc(percent, next.getLeftUpperLeg()));

        //Left lower Leg
        adjust(player.getBipedLeftLowerLeg(), prev.getLeftLowerLeg().calc(percent, next.getLeftLowerLeg()));
        adjust(player.getBipedLeftLowerLegwear(), prev.getLeftLowerLeg().calc(percent, next.getLeftLowerLeg()));

        //Head
        adjust(player.getBipedHead(), prev.getHead().calc(percent, next.getHead()));
        adjust(player.getBipedHeadwear(), prev.getHead().calc(percent, next.getHead()));

        //Chest
        adjust(player.getBipedBody(), prev.getChest().calc(percent, next.getChest()));
        adjust(player.getBipedBodywear(), prev.getChest().calc(percent, next.getChest()));


    }

    private void adjust(ModelRenderer renderer, BodyPart part) {
        if (part.rotationPointX != 0)
            renderer.rotationPointX = part.rotationPointX;

        if (part.rotationPointY != 0)
            renderer.rotationPointY = part.rotationPointY;

        if (part.rotationPointZ != 0)
            renderer.rotationPointZ = part.rotationPointZ;

        if (part.rotateAngleX != 0)
            renderer.rotateAngleX = part.rotateAngleX;

        if (part.rotateAngleY != 0)
            renderer.rotateAngleY = part.rotateAngleY;

        if (part.rotateAngleZ != 0)
            renderer.rotateAngleZ = part.rotateAngleZ;

        if (part.offsetX != 0)
            renderer.offsetX = part.offsetX;

        if (part.offsetY != 0)
            renderer.offsetY = part.offsetY;

        if (part.offsetZ != 0)

            renderer.offsetZ = part.offsetZ;

    }

    @Override
    public void modifyPlayer(AbstractClientPlayer entity, IMixinModelBiped player, float heldPercent) {
        if (!loaded)
            return;
        Long aLong = states.get(entity.getUniqueID());
        if (aLong == null || aLong == 0) {
            resetAnimation(player);
            return;
        }
        long current = System.currentTimeMillis();
        long timeSinceStart = current - aLong;

        AnimationFrame prev = null;
        AnimationFrame next = null;
        for (AnimationFrame frame : frames) {
            if (prev == null || (frame.getTime() < timeSinceStart && frame.getTime() > prev.getTime())) {
                prev = frame;
            }
            if ((next == null && frame.getTime() > prev.getTime()) || (frame.getTime() > timeSinceStart && frame.getTime() < next.getTime())) {
                next = frame;
            }
        }
        if (prev == null || next == null) {
            return;
        }

        float v = (timeSinceStart - prev.getTime());
        long l = next.getTime() - prev.getTime();
        float percent = v / (float) l;


//        next = frames.get(frames.size() - 1);
//        percent = 1.0F;
//        System.out.println(prev.name + " -> " + next.name + " " + percent);
//        Right upper arm
        adjust(player.getBipedRightUpperArm(), prev.getRightUpperArm().calc(percent, next.getRightUpperArm()));

        //Right lower arm
        adjust(player.getBipedRightForeArm(), prev.getRightLowerArm().calc(percent, next.getRightLowerArm()));

        //Left upper arm
        adjust(player.getBipedLeftUpperArm(), prev.getLeftUpperArm().calc(percent, next.getLeftUpperArm()));

        //Left lower arm
        adjust(player.getBipedLeftForeArm(), prev.getLeftLowerArm().calc(percent, next.getLeftLowerArm()));


        //Right upper Leg
        adjust(player.getBipedRightUpperLeg(), prev.getRightUpperLeg().calc(percent, next.getRightUpperLeg()));

        //Right lower Leg
        adjust(player.getBipedRightLowerLeg(), prev.getRightLowerLeg().calc(percent, next.getRightLowerLeg()));

        //Left upper Leg
        adjust(player.getBipedLeftUpperLeg(), prev.getLeftUpperLeg().calc(percent, next.getLeftUpperLeg()));

        //Left lower Leg
        adjust(player.getBipedLeftLowerLeg(), prev.getLeftLowerLeg().calc(percent, next.getLeftLowerLeg()));

        //Head
        adjust(player.getBipedHead(), prev.getHead().calc(percent, next.getHead()));
        adjust(player.getBipedHeadwear(), prev.getHead().calc(percent, next.getHead()));

        //Chest
        adjust(player.getBipedBody(), prev.getChest().calc(percent, next.getChest()));
    }
}
