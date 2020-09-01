package mg.didavid.firsttry.Models;

public class ModelRestoSampleMenu {
    private String id_resto, id_menu, menuIngredient, menuPhoto, menuName, menuPrice;

    public ModelRestoSampleMenu() {
    }

    public ModelRestoSampleMenu(String id_resto, String id_menu, String menuIngredient, String menuPhoto, String menuName, String menuPrice) {
        this.id_resto = id_resto;
        this.id_menu = id_menu;
        this.menuIngredient = menuIngredient;
        this.menuPhoto = menuPhoto;
        this.menuName = menuName;
        this.menuPrice = menuPrice;
    }

    public String getId_menu() {
        return id_menu;
    }

    public void setId_menu(String id_menu) {
        this.id_menu = id_menu;
    }

    public String getMenuIngredient() {
        return menuIngredient;
    }

    public void setMenuIngredient(String menuIngredient) {
        this.menuIngredient = menuIngredient;
    }

    public String getId_resto() {
        return id_resto;
    }

    public void setId_resto(String id_resto) {
        this.id_resto = id_resto;
    }

    public String getMenuPhoto() {
        return menuPhoto;
    }

    public void setMenuPhoto(String menuPhoto) {
        this.menuPhoto = menuPhoto;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuPrice() {
        return menuPrice;
    }

    public void setMenuPrice(String menuPrice) {
        this.menuPrice = menuPrice;
    }
}
