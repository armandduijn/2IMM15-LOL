<?php

namespace App;

interface RenderableInterface
{
    /**
     * @return string
     */
    public function getTitle(): string;

    /**
     * @param $data
     * @return string
     */
    public function render($data): string;
}
