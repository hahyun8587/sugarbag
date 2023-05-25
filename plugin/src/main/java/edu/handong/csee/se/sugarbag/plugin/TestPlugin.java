package edu.handong.csee.se.sugarbag.plugin;

import com.google.errorprone.annotations.Var;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.*;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.TargetType;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import javax.tools.JavaCompiler;
import java.util.*;
import java.util.stream.Collectors;

import static com.sun.tools.javac.util.List.nil;

public class TestPlugin implements Plugin {
    public static String NAME = "TestPlugin";

    private static Set<String> TARGET_TYPES = new HashSet<>(Arrays.asList(
            // Use only primitive types for simplicity
            byte.class.getName(), short.class.getName(), char.class.getName(),
            int.class.getName(), long.class.getName(), float.class.getName(), double.class.getName()));

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init(JavacTask task, String... args) {
        Context context = ((BasicJavacTask) task).getContext();
        task.addTaskListener(new TaskListener() {

            @Override
            public void finished(TaskEvent e) {
                if (e.getKind() != TaskEvent.Kind.PARSE) {
                    return;
                }

                e.getCompilationUnit().accept(new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void aVoid) {
                        return super.visitClass(node, aVoid);
                    }

                    @Override
                    public Void visitMethod(MethodTree method, Void v) {
                        List<VariableTree> parametersToInstrument
                                = method.getParameters().stream()
                                .filter(TestPlugin.this::shouldInstrument)
                                .collect(Collectors.toList());
                        if(!parametersToInstrument.isEmpty()) {
                            Collections.reverse(parametersToInstrument);
                            parametersToInstrument.forEach(p -> addCheck(method, p, context));
                        }
                        return super.visitMethod(method, v);
                    }
                }, null);
            }
        });
    }

    private boolean shouldInstrument(VariableTree parameter) {
        return TARGET_TYPES.contains(parameter.getType().toString())
                && parameter.getModifiers().getAnnotations()
                .stream()
                .anyMatch(a -> Positive.class.getSimpleName().equals(a.getAnnotationType().toString()));
    }

    private static JCTree.JCIf createCheck(VariableTree parameter, Context context) {
        TreeMaker factory = TreeMaker.instance(context);
        Names symbolsTable = Names.instance(context);

        return factory.at(((JCTree) parameter).pos)
                .If(factory.Parens(createIfCondition(factory, symbolsTable, parameter)),
                        createIfBlock(factory, symbolsTable, parameter),
                        null);
    }
}
