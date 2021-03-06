package team.unstudio.udpl.command.anno;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import team.unstudio.udpl.command.CommandHelper;
import team.unstudio.udpl.command.anno.CommandWrapper.OptionalWrapper;
import team.unstudio.udpl.command.anno.CommandWrapper.RequiredWrapper;
import team.unstudio.udpl.i18n.I18n;
import team.unstudio.udpl.util.extra.ASMClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Optional;
import java.util.logging.Level;

public class AnnoCommandManager implements CommandExecutor, TabCompleter {
    public static final CommandResultHandler DEFAULT_COMMAND_RESULT_HANDLER = new DefaultCommandResultHandler();
    public static final CommandParameterManager DEFAULT_COMMAND_PARAMETER_HANDLER = new DefaultCommandParameterManager();


    private final JavaPlugin plugin;
    private final String name;

    protected final CommandNode root;

    private final ASMClassLoader classLoader;

    private CommandResultHandler resultHandler;
    private CommandParameterManager parameterManager;

    private I18n i18n;
    private String usage;
    private String description;

    private final List<String> aliases = new ArrayList<>();

    /**
     * 创建指令管理者
     *
     * @param name 指令名
     */
    public AnnoCommandManager(String name) {
        this(name, (JavaPlugin) Bukkit.getPluginCommand(name).getPlugin());
    }

    /**
     * 创建指令管理者
     *
     * @param name   指令名
     * @param plugin 插件
     */
    public AnnoCommandManager(String name, JavaPlugin plugin) {
        this.name = Objects.requireNonNull(name);
        this.plugin = Objects.requireNonNull(plugin);
        this.root = new CommandNode(name, null);
        this.classLoader = new ASMClassLoader(plugin.getClass().getClassLoader());

        setResultHandler(DEFAULT_COMMAND_RESULT_HANDLER);
        setParameterManager(DEFAULT_COMMAND_PARAMETER_HANDLER);
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public I18n getI18n() {
        return i18n;
    }

    public void setI18n(I18n i18n) {
        this.i18n = i18n;
    }

    public CommandResultHandler getResultHandler() {
        return resultHandler;
    }

    public void setResultHandler(CommandResultHandler resultHandler) {
        resultHandler.setCommandManager(this);
        this.resultHandler = resultHandler;
    }

    public CommandParameterManager getParameterManager() {
        return parameterManager;
    }

    public void setParameterManager(CommandParameterManager parameterManager) {
        this.parameterManager = parameterManager;
    }

    public ASMClassLoader getClassLoader() {
        return classLoader;
    }

    public List<String> getAliases() {
        return aliases;
    }

    /**
     * 注册指令
     */
    public AnnoCommandManager registerCommand() {
        PluginCommand command = getPlugin().getCommand(getName());
        command.setExecutor(this);
        command.setTabCompleter(this);
        command.getAliases().addAll(aliases);
        getPlugin().getLogger().info("Register command \"" + getName() + "\" successful.");
        return this;
    }

    /**
     * 不安全的注册指令
     */
    public AnnoCommandManager unsafeRegisterCommand() {
        Optional<PluginCommand> command = CommandHelper.unsafeRegisterCommand(getName(), getPlugin());
        if (command.isPresent()) {
            command.get().setExecutor(this);
            command.get().setTabCompleter(this);
            command.get().getAliases().addAll(aliases);
            getPlugin().getLogger().info("Unsafe register command \"" + getName() + "\" successful.");
        } else {
            getPlugin().getLogger().warning("Unsafe register command \"" + getName() + "\" failure.");
        }
        return this;
    }

    /**
     * 添加指令
     */
    public AnnoCommandManager addHandler(Object object) {
        for (Method method : object.getClass().getMethods()) {
            try {
                team.unstudio.udpl.command.anno.Command command = method
                        .getAnnotation(team.unstudio.udpl.command.anno.Command.class);

                if (command != null) {
                    addCommand(createOrGetCommandNode(command.value()), object, method, command);

                    for (Alias alias : method.getAnnotationsByType(Alias.class))
                        addCommand(createOrGetCommandNode(alias.value()), object, method, command);

                    continue;
                }

                TabComplete tabComplete = method.getAnnotation(TabComplete.class);

                if (tabComplete != null) {

                    addTabComplete(createOrGetCommandNode(tabComplete.value()), object, method, tabComplete);

                    for (Alias alias : method.getAnnotationsByType(Alias.class))
                        addTabComplete(createOrGetCommandNode(alias.value()), object, method, tabComplete);
                }
            } catch (Exception e) {
                getPlugin().getLogger().log(Level.WARNING, e.getMessage(), e);
            }
        }
        return this;
    }

    protected void addCommand(CommandNode node, Object object, Method method,
                              team.unstudio.udpl.command.anno.Command command) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        node.getCommands().add(new CommandWrapper(node, this, object, method, command));
    }

    protected void addTabComplete(CommandNode node, Object object, Method method, TabComplete tabComplete) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        node.getTabCompleters().add(new TabCompleteWrapper(node, this, object, method, tabComplete));
    }

    public AnnoCommandManager addHandler(Object... objects) {
        for (Object object : objects)
            addHandler(object);
        return this;
    }

    public Optional<CommandNode> getCommandNode(String[] args) {
        CommandNode node = root;
        for (String arg : args) {
            CommandNode child = node.getChild(arg.toLowerCase());
            if (child == null)
                return Optional.empty();

            node = child;
        }

        return Optional.of(node);
    }

    protected CommandNode createOrGetCommandNode(String[] args) {
        CommandNode node = root;
        for (String arg : args) {
            CommandNode child = node.getChild(arg);
            if (child == null) {
                child = new CommandNode(arg, node);
                node.addChild(child);
            }

            node = child;
        }

        return node;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandNode node = root;

        int i, size;
        for (i = 0, size = args.length; i < size; i++) {
            CommandNode child = node.getChild(args[i]);
            if (child == null)
                break;

            node = child;
        }

        while (node != null && !node.hasCommand()) {
            node = node.getParent();
            i--;
        }

        if (node == null || !node.hasCommand()) {
            getResultHandler().onUnknownCommand(sender, args);
            return true;
        }

        handleCommand(node, sender, Arrays.copyOfRange(args, i, args.length));
        return true;
    }

    protected void handleCommand(CommandNode node, CommandSender sender, String args[]) {
        CommandWrapper command = getCommand(node, sender);
        if (command == null) {
            getResultHandler().onWrongSender(node, sender, args);
            return;
        }

        if (!command.checkPermission(sender)) {
            getResultHandler().onNoPermission(command, sender, args);
            return;
        }

        RequiredWrapper[] requireds = command.getRequireds();
        OptionalWrapper[] optionals = command.getOptionals();
        int argsLength = requireds.length + optionals.length;

        if (requireds.length > args.length) {
            getResultHandler().onNoEnoughParameter(command, sender, args);
            return;
        }

        if (command.isExactParameterMatching() && args.length > argsLength) {
            getResultHandler().onUnknownCommand(sender, args);
            return;
        }

        Object[] transformedRequireds = new Object[requireds.length];
        Object[] transformedOptionals = new Object[requireds.length];
        Set<Integer> wrongArgsIndex = new HashSet<>();

        for (int i = 0, size = requireds.length; i < size; i++) {
            try {
                transformedRequireds[i] = getParameterManager().transform(requireds[i].getType(), args[i]);
            } catch (Exception e) {
                wrongArgsIndex.add(i);
            }
        }

        for (int i = 0, j = requireds.length, size = optionals.length; i < size; i++, j++) {
            OptionalWrapper optional = optionals[i];
            try {
                transformedOptionals[i] = getParameterManager().transform(optional.getType(),
                        j < args.length ? args[j] : optional.getDefaultValue());
            } catch (Exception e) {
                wrongArgsIndex.add(j);
            }
        }

        if (wrongArgsIndex.size() > 0) {
            getResultHandler().onWrongParameter(command, sender, args, wrongArgsIndex);
            return;
        }

        String[] exactArgs = command.hasStringArray()
                ? argsLength < args.length ? Arrays.copyOfRange(args, argsLength, args.length) : new String[0]
                : null;

        try {
            command.invoke(sender, transformedRequireds, transformedOptionals, exactArgs);
        } catch (Exception e) {
            getResultHandler().onRunCommandFailure(command, sender, args, e);
        }
    }

    private CommandWrapper getCommand(CommandNode node, CommandSender sender) {
        for (CommandWrapper command : node.getCommands())
            if (command.checkSender(sender))
                return command;

        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        CommandNode node = root;
        String prefix = args[args.length - 1];
        List<String> tabComplete = new LinkedList<>();

        int i, size;
        for (i = 0, size = args.length - 1; i < size; i++) {
            CommandNode child = node.getChild(args[i]);
            if (child == null)
                break;

            node = child;
        }

        //Children
        for (CommandNode n : node.getChildren()) {
            if (n.getName().startsWith(prefix)) {
                CommandWrapper c = getCommand(n, sender);
                if (c != null && c.checkPermission(sender)) {
                    tabComplete.add(n.getName());
                }
            }
        }

        //TabCompleter
        CommandNode tabCompleterNode = node;
        int tabCompleterNodeIndex = i;
        while (tabCompleterNode != null && !tabCompleterNode.hasTabCompleter()) {
            tabCompleterNode = tabCompleterNode.getParent();
            tabCompleterNodeIndex--;
        }

        if (tabCompleterNode != null)
            tabComplete.addAll(handleTabComplete(tabCompleterNode, sender, Arrays.copyOfRange(args, tabCompleterNodeIndex, args.length)));

        //CommandComplete
        CommandNode commandNode = node;
        int commandNodeIndex = i;
        while (tabCompleterNode != null && !commandNode.hasCommand()) {
            commandNode = commandNode.getParent();
            commandNodeIndex--;
        }

        CommandWrapper commandWrapper = getCommand(commandNode, sender);
        if (commandWrapper != null && commandWrapper.checkPermission(sender)) {
            int tabCompleteIndex = args.length - commandNodeIndex;
            RequiredWrapper[] requireds = commandWrapper.getRequireds();
            OptionalWrapper[] optionals = commandWrapper.getOptionals();
            if (tabCompleteIndex < requireds.length + optionals.length) {
                List<String> typeResult;
                String[] complete;
                if (tabCompleteIndex < requireds.length) {
                    typeResult = getParameterManager().tabComplete(requireds[tabCompleteIndex].getType(), prefix);
                    complete = requireds[tabCompleteIndex].getComplete();
                } else {
                    typeResult = getParameterManager()
                            .tabComplete(optionals[tabCompleteIndex - requireds.length].getType(), prefix);
                    complete = optionals[tabCompleteIndex - requireds.length].getComplete();
                }
                if (typeResult != null)
                    tabComplete.addAll(typeResult);
                for (String s : complete)
                    if (s.startsWith(prefix))
                        tabComplete.add(s);
            }
        }
        return tabComplete;
    }

    protected List<String> handleTabComplete(CommandNode node, CommandSender sender, String args[]) {
        TabCompleteWrapper tabComplete = getTabComplete(node, sender);

        if (tabComplete == null)
            return Collections.emptyList();

        if (!tabComplete.checkPermission(sender))
            return Collections.emptyList();

        try {
            List<String> result = tabComplete.invoke(sender, args);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            getPlugin().getLogger().log(Level.WARNING, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private TabCompleteWrapper getTabComplete(CommandNode node, CommandSender sender) {
        for (TabCompleteWrapper tabComplete : node.getTabCompleters())
            if (tabComplete.checkSender(sender))
                return tabComplete;

        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private JavaPlugin plugin;
        private final List<CommandParameterHandler> parameterHandlers = new ArrayList<>();
        private I18n i18n;
        private String usage;
        private String description;
        private CommandResultHandler resultHandler;
        private final List<String> aliases = new ArrayList<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder plugin(JavaPlugin plugin) {
            this.plugin = plugin;
            return this;
        }

        public Builder usage(String usage) {
            this.usage = usage;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder i18n(I18n i18n) {
            this.i18n = i18n;
            return this;
        }

        public Builder parameterHandler(CommandParameterHandler handler) {
            parameterHandlers.add(handler);
            return this;
        }

        public Builder resultHandler(CommandResultHandler handler) {
            resultHandler = handler;
            return this;
        }

        public Builder alias(String alias) {
            aliases.add(alias);
            return this;
        }

        public Builder aliases(String... aliases) {
            Collections.addAll(this.aliases, aliases);
            return this;
        }

        public AnnoCommandManager build() {
            AnnoCommandManager manager = new AnnoCommandManager(name, plugin);
            manager.setI18n(i18n);
            manager.setUsage(usage);
            manager.setDescription(description);
            if (resultHandler != null) manager.setResultHandler(resultHandler);
            if (parameterHandlers.size() > 0) manager.setParameterManager(new DefaultCommandParameterManager(parameterHandlers));
            return manager;
        }
    }
}
