package aurora.parser.argument;

import aurora.file.AsmFile;
import aurora.parser.ParsedPath;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/*
 * @project aurora
 * @author Gabriel Honda on 19/02/2020
 */
public class Argument {

    public static ParsedPath parseArgs(String[] args) {
        Path asmPath, auroraPath;
        final String auroraExt = ".au";
        final String asmExt = ".asm";

        // converte os argumentos para ArrayList para utilizar Stream
        List<String> listArgs = activateFlags(args);

        // assume-se que um arquivo aurora deve-se ter a extensao .au,
        // procura dentro da lista uma string que termina com .au
        Optional<String> _au = findFile(listArgs, arg -> arg.endsWith(auroraExt));

        Optional<String> _asm = findFile(listArgs, arg -> arg.endsWith(asmExt));

        auroraPath = Path.of(_au.orElseThrow(() -> new IllegalArgumentException("Não foi encontrado o arquivo do tipo" + auroraExt)));

        asmPath = processAsmPath(auroraPath, _asm);

        // retorna uma instancia de ParsedPath que possui a informacao do caminho
        // dos arquivos de input e output
        // que serao usados por outras classes
        return new ParsedPath(auroraPath, asmPath);
    }

    private static List<String> activateFlags(String[] args) {
        List<String> listArgs = Arrays.asList(args);

        if(listArgs.contains("--all")) {
            // ativa todas as flags
            Stream.of(Flag.values())
                    .forEach(f -> f.setValue(true));
        } else {
            listArgs.stream()
                    // filtra as Strings dentro de listArgs checando se estao contidas dentro
                    // da lista E se comecam com '--' que sinaliza o inicio de uma flag
                    .filter(arg -> listArgs.contains(arg) && arg.startsWith("--"))
                    .forEach(arg -> Flag.getFlag(arg).setValue(true));
        }
        return listArgs;
    }
    private static Path processAsmPath(Path auroraPath, Optional<String> _asm) {
        Path asmPath;
        if(_asm.isEmpty()){
            asmPath = AsmFile.create(auroraPath);
        } else {
            asmPath = Path.of(_asm.get());
        }
        return asmPath;
    }
    private static Optional<String> findFile(List<String> list, Predicate<String> constraint) {
        return list.stream()
                .filter(constraint)     // executa o metodo de teste
                .findFirst();           // recupera o primeiro resultado que é um Optional
    }

}
