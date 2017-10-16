<?php

namespace App;

class Helper
{

    /**
     * Runs a Python command
     *
     * @param string $script
     * @param string $argument
     * @return string
     */
    public static function runCommand(string $script, string $argument): string
    {
        $command = sprintf(
            'python "%s/../../../modules/view-helpers/%s" %s',
            __DIR__,
            $script,
            escapeshellarg($argument)
        );

        return shell_exec($command);
    }

    /**
     * Render a template
     *
     * @param string $template Absolute path to the template file
     * @param array  $data     Data to be passed to the template file
     *
     * @return string
     */
    public static function render(string $template, array $data = []): string
    {
        ob_start();

        extract($data);
        include $template;

        return ob_get_clean();
    }
}
