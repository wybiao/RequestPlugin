package action;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiUtilBase;

public class RequestCreatorAction extends BaseGenerateAction {

    public RequestCreatorAction() {
        super(null);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        PsiClass targetClass = getTargetClass(editor, file);
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                StringBuilder sbField = new StringBuilder();
                StringBuilder sbAssignment = new StringBuilder();
                PsiField[] fields = targetClass.getFields();
                for (PsiField field : fields) {
                    if (sbField.length() != 0) {
                        sbField.append(",");
                    }
                    sbField.append(field.getType().getCanonicalText() + " " + field.getName());
                    sbAssignment.append("this." + field.getName() + " = " + field.getName() + ";\n");
                    PsiElement element = factory.createAnnotationFromText("@FieldName(\"" + field.getName() + "\")\n", field);
                    targetClass.addBefore(element, field);
                }
                StringBuilder sbConstructor = new StringBuilder();
                sbConstructor.append("public " + targetClass.getName() + "(");
                sbConstructor.append(sbField);
                sbConstructor.append("){\n");
                sbConstructor.append(sbAssignment);
                sbConstructor.append("}");
                targetClass.add(factory.createMethodFromText(sbConstructor.toString(), targetClass));
                styleManager.optimizeImports(file);
                styleManager.shortenClassReferences(targetClass);
            }
        });
    }
}
