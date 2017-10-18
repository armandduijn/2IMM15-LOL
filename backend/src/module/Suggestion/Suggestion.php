<?php

namespace App\Suggestion;

require_once(__DIR__."/../Stemming/Porter.php");

use App\AbstractModule;
use App\RenderableInterface;
use PDO;
use App\Stemming\Porter2 as Porter;

class Suggestion extends AbstractModule
{
    public function getTitle(): string
    {
        return 'Suggestion';
    }
    
    public function giveSuggestions($query) {
        // parse query
        $pieces = explode(' ', $query);
        $lastWord = array_pop($pieces);
        
        // correct last word
        $command = "python \"" . __DIR__ . "/../../../../modules/view-helpers/spelling.py\" ";
        $command .= escapeshellarg($lastWord);
        $correctedLastWord = trim(shell_exec($command));
//        var_dump($correctedLastWord);die;
        
        $stem = Porter::stem($correctedLastWord);
        
        $db = new PDO('sqlite:'.__DIR__.'/../../../../data/derived/bigram.sqlite');
        $statement = $db->prepare('SELECT suggestion, count FROM bigram WHERE stem = :stem ORDER BY count DESC LIMIT 10');
        $statement->execute([':stem' => $stem]);
        $suggestions = [];
        
        while ($row = $statement->fetch(PDO::FETCH_ASSOC)) {
            $suggestions[] = join(" ", $pieces)." ".$correctedLastWord." ".$row['suggestion'];
        }
        
        return $suggestions;
    }
}