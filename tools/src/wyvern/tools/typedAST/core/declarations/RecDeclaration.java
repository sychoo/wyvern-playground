package wyvern.tools.typedAST.core.declarations;

import java.util.LinkedList;
import java.util.List;

import wyvern.target.corewyvernIL.BindingSite;
import wyvern.target.corewyvernIL.decltype.DeclType;
import wyvern.target.corewyvernIL.decltype.RecDeclType;
//import wyvern.target.corewyvernIL.expression.Expression;
import wyvern.target.corewyvernIL.expression.RecExpression;
import wyvern.target.corewyvernIL.expression.RecExpression;
import wyvern.target.corewyvernIL.modules.TypedModuleSpec;
import wyvern.target.corewyvernIL.support.GenContext;
import wyvern.target.corewyvernIL.support.TopLevelContext;
import wyvern.target.corewyvernIL.type.StructuralType;
import wyvern.target.corewyvernIL.type.ValueType;
import wyvern.tools.typedAST.interfaces.ExpressionAST;
import wyvern.tools.types.Type;
import wyvern.tools.errors.FileLocation;
import wyvern.tools.typedAST.abs.Declaration;
import wyvern.tools.typedAST.core.Sequence;
import wyvern.tools.typedAST.core.binding.NameBinding;
import wyvern.tools.typedAST.interfaces.CoreAST;
//import wyvern.tools.typedAST.interfaces.ExpressionAST;
import wyvern.tools.typedAST.interfaces.TypedAST;
import wyvern.tools.typedAST.typedastvisitor.TypedASTVisitor;

public class RecDeclaration extends Declaration implements CoreAST {
    private List<TypedAST> body;
    private ExpressionAST original_body;
    private String variableName; // generated fresh variable
    private List<NameBinding> bodyNameBindings; // store the name binding of the bodies
    private FileLocation location = FileLocation.UNKNOWN;
    private BindingSite bindingSite;

    public RecDeclaration(TypedAST body, FileLocation location) {
        this.original_body = (ExpressionAST) body;
        this.body = ((Sequence) body).getExps();
        this.location = location;
        this.variableName = GenContext.generateName(); // generate fresh variable
        this.bindingSite = new BindingSite(this.variableName);
        System.out.println();
        System.out.println("RecDeclaration Body: " + this.body); // debugger
        System.out.println();
        System.out.println("RecDeclaration Variable: " + this.variableName);
    }

    @Override
    public <S, T> T acceptVisitor(TypedASTVisitor<S, T> visitor, S state) {
        // TODO Auto-generated method stub
        return visitor.visit(state, this);
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return this.variableName;
    }

    @Override
    public DeclType genILType(GenContext ctx) {
        // TODO Auto-generated method stub
        // VarBinding() list
        ctx = this.extendContext(ctx);
        RecDeclType returnObject = new RecDeclType(this.getName(), getILValueType(ctx) /* to be debugger*/); //debugger

        return returnObject;
    }

    @Override
    public wyvern.target.corewyvernIL.decl.Declaration generateDecl(GenContext ctx, GenContext thisContext) {
        ValueType expectedType = this.getILValueType(thisContext);
        /* uses ctx for generating the definition, as the selfName is not in scope */
        return new wyvern.target.corewyvernIL.decl.RecDeclaration(this.getName(), expectedType, this.original_body.generateIL(ctx, expectedType, null), location);
    }


    @Override
    public wyvern.target.corewyvernIL.decl.Declaration topLevelGen(GenContext ctx, List<TypedModuleSpec> dependencies) {
        return generateDecl(ctx, ctx);
    }

    public BindingSite getSite() {
        return this.bindingSite;
    }

    @Override
    public void genTopLevel(TopLevelContext tlc) {
        ValueType declType = getILValueType(tlc.getContext());
        System.out.println(this.original_body.generateIL(tlc.getContext(), declType, tlc.getDependencies()));

        // debugger
        tlc.addLet(this.getSite(),
                getILValueType(tlc.getContext()),
                this.original_body.generateIL(tlc.getContext(), declType, tlc.getDependencies()),
                true);
    }

    @Override
    public FileLocation getLocation() {
        // TODO Auto-generated method stub
        return this.location;
    }

    @Override
    public void addModuleDecl(TopLevelContext tlc) {
    }

    private GenContext extendContext(GenContext ctx) {
        String constructVariableName;
        Type type;
        ValueType valueType;
        for (TypedAST arg : this.body) {
            constructVariableName = ((RecConstructDeclaration) arg).getName();
            type = ((RecConstructDeclaration) arg).getType();
            valueType = type.getILType(ctx);
            ctx = ctx.extend(constructVariableName, new RecExpression(/*to be debugger*/getSite(), valueType), valueType);
        }
        return ctx;
    }

    private ValueType getILValueType(GenContext ctx) {
        List <DeclType> typeList = new LinkedList<DeclType>();

        for (TypedAST arg : this.body) {
            typeList.add(((RecConstructDeclaration) arg).genILType(ctx));
        }

        StructuralType valueType = new StructuralType(this.getSite(), typeList, false, this.getLocation());
        return valueType;
    }
}
