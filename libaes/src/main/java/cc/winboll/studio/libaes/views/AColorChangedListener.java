package cc.winboll.studio.libaes.views;

/**
 * Color change callback shared by AColorPickerView and AColorPickerLayout.
 * beforeColorChanged/afterColorChanged bracket a touch gesture so the host
 * can suppress feedback while the picker is being dragged.
 */
public interface AColorChangedListener {

    void onColorChanged(int color);

    void beforeColorChanged();

    void afterColorChanged();
}
