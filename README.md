# Android view recycling reuses a view that still has a parent

With `enableViewRecycling` on, `ViewManager.onDropViewInstance` pools a view
even when `view.getParent() != null`. A view keeps its parent after removal when
the parent called `ViewGroup.startViewTransition(child)` first, as exit
animations do. If that parent leaves the window before it calls
`endViewTransition`, `ViewGroup.dispatchDetachedFromWindow` empties its
disappearing children but leaves the child's parent set, and nothing can clear
it after that. When the pool hands that view out again, `addViewAt` crashes with
"The specified child already has a parent".

[react/react-native#59776](https://github.com/react/react-native/pull/59776)
doesn't help here: its `endViewTransition` call only clears the parent while the
old parent still lists the view as a disappearing child.

Filed as [react/react-native#59863](https://github.com/react/react-native/issues/59863).

[`TransitionContainer`](ReproducerApp/android/app/src/main/java/com/reproducerapp/TransitionContainer.kt)
is a small native container that keeps a removed child drawn for a 1 second exit
animation, like react-native-screens does for a screen that is popped.

- react-native `0.90.0-nightly-20261108-f480836ad` (`main` at `f480836ad`,
  which includes #59776), New Architecture, Hermes
- Android emulator, API 36

## Run it

```sh
cd ReproducerApp
yarn install
yarn android --mode release   # or `yarn android` for a debug build with Metro
```

## Steps

Press **Run**. It:

1. removes the red view from `TransitionContainer`, which starts its exit
   animation,
2. 300 ms later, removes `TransitionContainer`,
3. 300 ms later, mounts ten new views.

The switch at the top turns `enableViewRecycling` on or off and restarts the
app. It's on by default.

**Recycling on:** the release build crashes (a debug build shows the same error
in a redbox). The first new view is the red view from step 1, taken from the
pool with `TransitionContainer` still set as its parent:

```
E SurfaceMountingManager: java.lang.IllegalStateException: addViewAt: cannot insert view [54] into parent [1]: View already has a parent: [24] Parent: TransitionContainer View: ReactViewGroup
E AndroidRuntime: FATAL EXCEPTION: main
E AndroidRuntime: java.lang.IllegalStateException: addViewAt: failed to insert view [54] into parent [1] at index 13
    at com.facebook.react.fabric.mounting.SurfaceMountingManager.addViewAt(SurfaceMountingManager.kt:376)
    at com.facebook.react.fabric.mounting.mountitems.IntBufferBatchMountItem.execute(IntBufferBatchMountItem.kt:122)
    ...
Caused by: java.lang.IllegalStateException: The specified child already has a parent. You must call removeView() on the child's parent first.
    at android.view.ViewGroup.addViewInner(ViewGroup.java:5289)
    at android.view.ViewGroup.addView(ViewGroup.java:5118)
    at android.view.ViewGroup.addView(ViewGroup.java:5058)
    at com.facebook.react.uimanager.ViewGroupManager.addView(ViewGroupManager.kt:29)
    at com.facebook.react.uimanager.ViewGroupManager.addView(ViewGroupManager.kt:15)
    at com.facebook.react.fabric.mounting.SurfaceMountingManager.addViewAt(SurfaceMountingManager.kt:373)
```

**Recycling off:** ten green squares appear. Once the exit animation ends,
`TransitionContainer` logs that `endViewTransition` left the parent set:

```
D TransitionContainer: exit animation of view 22 ended, attached to window: false, its parent is com.reproducerapp.TransitionContainer{...}
```

## Fix

In `ViewManager.onDropViewInstance`, don't pool a view that still has a parent:

```java
if (view.getParent() != null) {
  return;
}
```
